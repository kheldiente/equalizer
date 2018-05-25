package midien.kheldiente.equalizer

import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.media.audiofx.Equalizer
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_switch.*
import midien.kheldiente.equalizer.adapter.PresetAdapter
import midien.kheldiente.equalizer.data.Preset
import midien.kheldiente.equalizer.view.EqualizerView
import org.json.JSONObject

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener, EqualizerView.EventListener {

    private val TAG = MainActivity::class.java.simpleName

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var presetAdapter: PresetAdapter? = null
    private var cachedBandSettings: JSONObject? = null
    private val presetList = ArrayList<Preset>(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        startMediaPlayer()
    }
    private fun init() {
        setSupportActionBar(tb_app)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupMedia()
        setupEqualizerView()
        setupPresetList()
        setupPreviousSettings()
    }

    private fun setupMedia() {
        val eqEnabled = AppSettings.getSettingAsBoolean(this, AppSettings.EQUALIZER_ENABLED)
        mediaPlayer = MediaPlayer.create(this, R.raw.htmlthesong)
        mediaPlayer?.isLooping = true

        equalizer = Equalizer(0, mediaPlayer?.audioSessionId!!)
        equalizer?.enabled = eqEnabled
    }

    private fun setupEqualizerView() {
        val numberOfBands = equalizer?.numberOfBands
        val lowestBandLevel = equalizer?.bandLevelRange?.get(0)
        val highestBandLevel = equalizer?.bandLevelRange?.get(1)
        val max = highestBandLevel?.minus(lowestBandLevel!!)!!
        Log.d(TAG, "Cached band settings: $cachedBandSettings")
        Log.d(TAG, "Number of bands: $numberOfBands")
        Log.d(TAG, "Lowest band level: ${lowestBandLevel?.div(100)}dB")
        Log.d(TAG, "Highest band level: ${highestBandLevel?.div(100)}dB")
        Log.d(TAG, "Max level: ${max}dB")

        var bands = ArrayList<Integer>(0)
        // Get center frequency for each band
        (0 until numberOfBands!!)
                .map { equalizer?.getCenterFreq(it.toShort()) }
                .mapTo(bands) { Integer(it?.div(1000)!!) }
                .forEach { Log.d(TAG, "Center frequency: $it Hz") }

        view_eq.setBands(bands)
        view_eq.setMax(max)
        view_eq.setBandListener(this)
        view_eq.draw() // Force draw with new equalizer settings
    }

    private fun setupPresetList() {
        val eqEnabled = AppSettings.getSettingAsBoolean(this, AppSettings.EQUALIZER_ENABLED)
        var eqPreset = AppSettings.getSettingAsString(this, AppSettings.EQUALIZER_PRESET)
        if(eqPreset.isEmpty())
            eqPreset = "Normal" // Override preset

        switch_equalizer.setOnCheckedChangeListener(this)
        switch_equalizer.isChecked = eqEnabled

        presetAdapter = PresetAdapter(this) { position: Int, preset: Preset ->
            setSelectedPreset(position, preset)
        }
        presetAdapter?.enabled = eqEnabled
        presetAdapter?.currentPreset = eqPreset

        list_preset.layoutManager = LinearLayoutManager(this)
        list_preset.isNestedScrollingEnabled = true
        list_preset.adapter = presetAdapter

        presetList.clear()
        val presets = equalizer?.numberOfPresets
        // Get preset names
        (0 until presets!!)
                .map { equalizer?.getPresetName(it.toShort()) }
                .mapTo(presetList) { Preset(it) }
                .run {
                    // Add "User" preset
                    presetList.add(Preset("User"))
                    presetAdapter?.presetList = presetList
                    presetAdapter?.notifyDataSetChanged()
                }
    }

    private fun setupPreviousSettings() {
        cachedBandSettings = AppSettings.getSettingList(this, AppSettings.EQUALIZER_BAND_SETTINGS)

        val numberOfBands = equalizer?.numberOfBands
        for(i in 0 until numberOfBands!!) {
            if(cachedBandSettings?.has(i.toString())!!) {
                val cacheLevel = cachedBandSettings?.getString(i.toString())?.toInt()
                val lowestBandLevel = equalizer?.bandLevelRange?.get(0)
                val bandLevel = cacheLevel?.plus(lowestBandLevel!!)

                Log.d(TAG, "Cached value => band: $i, level: $bandLevel")
                setBandLevel(i.toShort(), bandLevel!!.toShort())
                view_eq.setBandLevel(i, cacheLevel!!)
            }
        }

    }

    private fun enableEqualizer(enable: Boolean) {
        equalizer?.enabled = enable
    }

    private fun setSelectedPreset(position: Int, preset: Preset) {
        var currentEqPreset = AppSettings.getSettingAsString(this, AppSettings.EQUALIZER_PRESET)

        if(currentEqPreset !== preset.name) {
            AppSettings.setSetting(this, AppSettings.EQUALIZER_PRESET, preset.name!!)

            val presets = equalizer?.numberOfPresets
            Log.d(TAG, "setSelectedPreset => presets: $presets, position: $position")
            if(position < presets!!.toInt()) {
                equalizer?.usePreset(position.toShort())
                // Get the number of frequency bands for this equalizer engine
                val numberOfBands = equalizer?.numberOfBands
                val lowestBandLevel = equalizer?.bandLevelRange?.get(0)

                (0 until numberOfBands!!)
                        .forEach {
                            val band = it
                            val level = equalizer?.getBandLevel(it.toShort())?.minus(lowestBandLevel!!)
                            Log.d(TAG, "setSelectedPreset => band: $it, level: $level")
                            view_eq.setBandLevel(band, level!!)
                        }
            } else {
                presetAdapter?.currentPreset = preset.name
                presetAdapter?.check(position)
            }
        }
    }

    private fun startMediaPlayer() {
        mediaPlayer?.isPlaying?.let {
            // Execute if not null
            if(!it)
                mediaPlayer?.start()
        } ?: run {
            // Execute if null
            init()
            startMediaPlayer()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.isPlaying?.let {
            if(it)
                mediaPlayer?.stop()

            mediaPlayer?.release()
            mediaPlayer = null
        }

        equalizer?.release()
        equalizer = null
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, checked: Boolean) {
        AppSettings.setSetting(this, AppSettings.EQUALIZER_ENABLED, checked)
        enableEqualizer(checked)
        presetAdapter?.enableAll(checked)
    }

    override fun onBandLevelChanged(bandId: Int, value: Int, fromUser: Boolean) {
        val lowestBandLevel = equalizer?.bandLevelRange?.get(0)
        val bandLevel = (value.plus(lowestBandLevel!!)).toShort()

        Log.d(TAG, "bandId: $bandId, bandLevel: $bandLevel, fromUser: $fromUser ")
        // Save to cache. The one saved to cached is the value and NOT the bandLevel
        AppSettings.addSettingToList(this, bandId.toString(), value)
        // Manipulate equalizer band level
        setBandLevel(bandId.toShort(), bandLevel)

        // If user suddenly move the band levels, set preset as 'User'
        if(fromUser) {
            // Assign selected preset as 'User'
            val position = presetList.indexOf(presetList.filter { preset -> preset.name.equals("User") }[0])
            val preset = presetList.filter { preset -> preset.name.equals("User") }[0]
            setSelectedPreset(position, preset)
        }
    }

    private fun setBandLevel(bandId: Short, level: Short) {
        equalizer?.setBandLevel(bandId, level)
    }

    override fun onResume() {
        super.onResume()
        startMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        stopMediaPlayer()
    }

}
