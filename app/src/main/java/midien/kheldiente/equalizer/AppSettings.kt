package midien.kheldiente.equalizer

import android.content.Context
import android.content.SharedPreferences

object AppSettings {

    private val PREF_NAME = "settings_pref"

    // Settings
    val EQUALIZER_ENABLED = "settings_eq_enabled"
    val EQUALIZER_PRESET = "settings_eq_preset"

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

}