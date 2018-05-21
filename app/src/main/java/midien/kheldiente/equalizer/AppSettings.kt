package midien.kheldiente.equalizer

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object AppSettings {

    private val PREF_NAME = "settings_pref"

    // Settings
    val EQUALIZER_ENABLED = "settings_eq_enabled"
    val EQUALIZER_PRESET = "settings_eq_preset"
    val EQUALIZER_BAND_SETTINGS = "settings_band_level";

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setSetting(context: Context, key: String, value: String) {
        val preferences = getPrefs(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setSetting(context: Context, key: String, value: Boolean) {
        val preferences = getPrefs(context)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getSettingAsBoolean(context: Context, key: String): Boolean {
        return getPrefs(context).getBoolean(key, false)
    }

    fun getSettingAsString(context: Context, key: String): String {
        return getPrefs(context).getString(key, "")
    }

    private fun setSettingList(context: Context, key: String, list: JSONObject) {
        val preferences = getPrefs(context)
        val editor = preferences.edit()
        editor.putString(key, list.toString())
        editor.apply()
    }

    fun getSettingList(context: Context, key: String): JSONObject{
        return JSONObject(getPrefs(context).getString(key, JSONObject().toString()))
    }

    fun addSettingToList(context: Context, key: String, value: Any) {
        val settingList = getSettingList(context, EQUALIZER_BAND_SETTINGS)
        settingList.put(key, value)
        setSettingList(context, EQUALIZER_BAND_SETTINGS, settingList)
    }

}