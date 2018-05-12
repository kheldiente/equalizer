package midien.kheldiente.equalizer.view

import android.content.Context

object CacheManager {

    val DEFAULT_CACHE_SIZE = 1024 * 1024 * 4 // 4 megs
    private var custFontCache: FontCache? = null

    fun getCustomFontCache(context: Context): FontCache? {
        if (custFontCache == null) {
            custFontCache = FontCache(DEFAULT_CACHE_SIZE)
        }
        return custFontCache
    }

}

