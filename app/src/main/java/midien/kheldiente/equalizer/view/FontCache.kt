package midien.kheldiente.equalizer.view

import android.graphics.Typeface
import android.support.v4.util.LruCache

class FontCache(maxSize: Int) : LruCache<String, Typeface>(maxSize)