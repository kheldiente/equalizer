package midien.kheldiente.equalizer.util

import android.content.Context


class PixelUtil {

    companion object {

        fun pxToDp(context: Context, px: Float): Float {
            return px / context.resources.displayMetrics.density
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return dp *  context.resources.displayMetrics.density
        }

    }

}