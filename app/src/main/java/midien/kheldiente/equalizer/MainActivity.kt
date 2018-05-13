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
import android.widget.Switch
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

    private fun init() {

        setSupportActionBar(tb_app)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mediaPlayer = MediaPlayer.create(this, R.raw.htmlthesong)
        mediaPlayer?.isLooping = true

        equalizer = Equalizer(0, mediaPlayer?.audioSessionId!!)
        equalizer?.enabled = true

        setupEqualizerView()
        setupPresetList()
    }

    private fun setupEqualizerView() {
        val numberOfBands = equalizer?.numberOfBands
        val lowestBandLevel = equalizer?.bandLevelRange?.get(0)?.div(100) // in decibels
        val highestBandLevel = equalizer?.bandLevelRange?.get(1)?.div(100) // in decibels

        Log.d(TAG, String.format("Number of bands: %s", numberOfBands))
        Log.d(TAG, String.format("Lowest band level: %s dB", lowestBandLevel))
        Log.d(TAG, String.format("Highest band level: %s dB", highestBandLevel))

        var bands = ArrayList<Integer>(0)
        // Get center frequency for each band
        (0 until numberOfBands!!)
                .map { equalizer?.getCenterFreq(it.toShort()) }
                .mapTo(bands) { Integer(it?.div(1000)!!) }
                .forEach { Log.d(TAG, String.format("Center frequency: %sHz", it)) }

        view_eq.setBands(bands)
    }

    private fun setupPresetList() {
        switch_equalizer.setOnCheckedChangeListener(this)

        presetAdapter = PresetAdapter(this) {
            // Toast.makeText(this, "${it.name} Clicked", Toast.LENGTH_SHORT).show()
        }

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
