package midien.kheldiente.equalizer

import android.Manifest
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.pm.PackageManager
import android.media.audiofx.Equalizer
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_switch.*
import midien.kheldiente.equalizer.adapter.PresetAdapter
import midien.kheldiente.equalizer.data.Preset

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private val TAG = MainActivity::class.java.simpleName
    private val PERMISSION_RECORD_AUDIO_REQUEST_CODE = 88

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var presetAdapter: PresetAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        setupPermissions()
    }

    private fun setupMedia() {
        mediaPlayer = MediaPlayer.create(this, R.raw.htmlthesong)
        mediaPlayer?.isLooping = true

        equalizer = Equalizer(0, mediaPlayer?.audioSessionId!!)
        equalizer?.enabled = true
    }

    private fun init() {
        setSupportActionBar(tb_app)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupMedia()
        setupEqualizerView()
        setupPresetList()
    }

    private fun setupEqualizerView() {
        val numberOfBands = equalizer?.numberOfBands
        val lowestBandLevel = equalizer?.bandLevelRange?.get(0)
        val highestBandLevel = equalizer?.bandLevelRange?.get(1)
        val max = highestBandLevel?.minus(lowestBandLevel!!)!!
        Log.d(TAG, "Number of bands: $numberOfBands")
        Log.d(TAG, "Lowest band level: ${lowestBandLevel?.div(100)}dB")
        Log.d(TAG, "Highest band level: ${highestBandLevel?.div(100)}dB")
        Log.d(TAG, "Max level: ${max}dB")

        var bands = ArrayList<Integer>(0)
        // Get center frequency for each band
        (0 until numberOfBands!!)
                .map { equalizer?.getCenterFreq(it.toShort()) }
                .mapTo(bands) { Integer(it?.div(1000)!!) }
                .forEach { Log.d(TAG, "Center frequency: {$it}Hz") }

        view_eq.setBands(bands)
        view_eq.setMax(max)
    }

    private fun setupPresetList() {
        val eqEnabled = AppSettings.getSettingAsBoolean(this, AppSettings.EQUALIZER_ENABLED)
        var eqPreset = AppSettings.getSettingAsString(this, AppSettings.EQUALIZER_PRESET)
        if(eqPreset.isEmpty())
            eqPreset = "Normal" // Override preset

        switch_equalizer.setOnCheckedChangeListener(this)
        switch_equalizer.isChecked = eqEnabled

        presetAdapter = PresetAdapter(this) {
            // Toast.makeText(this, "${it.name} Clicked", Toast.LENGTH_SHORT).show()
            AppSettings.setSetting(this, AppSettings.EQUALIZER_PRESET, it.name!!)
        }
        presetAdapter?.enabled = eqEnabled

        presetAdapter?.currentPreset = eqPreset

        list_preset.layoutManager = LinearLayoutManager(this)
        list_preset.isNestedScrollingEnabled = true
        list_preset.adapter = presetAdapter

        val presets = equalizer?.numberOfPresets
        // Get preset names
        val presetList = ArrayList<Preset>(0)
        (0 until presets!!)
                .map { equalizer?.getPresetName(it.toShort()) }
                .mapTo(presetList) { Preset(it) }
                .run {
                    presetAdapter?.presetList = presetList
                    presetAdapter?.notifyDataSetChanged()
                }
    }



    private fun startMediaPlayer() {
        mediaPlayer?.isPlaying?.let {
            // Execute if not null
            if(!it!!)
                mediaPlayer?.start()
        } ?: run {
            // Execute if null
            init()
            startMediaPlayer()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.isPlaying?.let {
            if(it!!)
                mediaPlayer?.stop()

            mediaPlayer?.release()
            mediaPlayer = null
        }

        equalizer?.release()
        equalizer = null
    }

    private fun setupPermissions() {
        // If we don't have the record audio permission...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                val permissionsWeNeed = arrayOf(Manifest.permission.RECORD_AUDIO)
                requestPermissions(permissionsWeNeed, PERMISSION_RECORD_AUDIO_REQUEST_CODE)
            }
        } else {
            startMediaPlayer()
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, checked: Boolean) {
        AppSettings.setSetting(this, AppSettings.EQUALIZER_ENABLED, checked)
        presetAdapter?.enableAll(checked)
    }

    override fun onResume() {
        super.onResume()
        startMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        stopMediaPlayer()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_RECORD_AUDIO_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The permission was granted! Start up the visualizer!
                    startMediaPlayer()
                } else {
                    Toast.makeText(this, "Permission for audio not granted. Visualizer can't run.", Toast.LENGTH_LONG).show()
                    finish()
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }
        }
        // Other permissions could go down here
    }

}
