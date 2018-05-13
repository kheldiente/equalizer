package midien.kheldiente.equalizer.util

import android.content.Context


object PixelUtil {

    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return dp *  context.resources.displayMetrics.density
    }

    fun pxToDp(context: Context, px: Int): Float {
        return px / context.resources.displayMetrics.density
    }

    fun dpToPx(context: Context, dp: Int): Float {
        return dp *  context.resources.displayMetrics.density
    }

}