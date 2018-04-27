package midien.kheldiente.equalizer.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.SeekBar
import kotlin.collections.ArrayList
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import android.view.View
import midien.kheldiente.equalizer.R


class EqualizerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
): ViewGroup(context, attrs, defStyle, defStyleRes) {

    private var bandSize = 3

    private val bandList: ArrayList<BandView> = ArrayList(bandSize)

    init {
        attrs?.let {
            val typedArray = context.theme.obtainStyledAttributes(attrs,
                    R.styleable.EqualizerView,
                    0,
                    0)
            // Get set attr value for bands
            bandSize = typedArray.getInteger(R.styleable.EqualizerView_bands, bandSize)
            typedArray.recycle()

            initDefaults()
        }
    }

    private fun initDefaults() {
        // Set canvas styles here
        // setBackgroundColor(Color.BLACK)

        // Add default (3) band views
        for(i in 1..bandSize) {
            val bv = BandView(context)
            // Add to list for reference
            bandList.add(bv)
            // Add to display
            addView(bv)
        }
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Warning! In order to set bands vertically,
        // the one to manipulate is not the height, but the WIDTH!
        // Because the band is ROTATED 270 degrees!
        setBandsVertically(w, h)
    }

    private fun setBandsVertically(width: Int, height: Int) {
        // TODO This method only works on 3 bands! Make it flexible by using other numbers!
        val distW = width / bandSize
        val distH = height / bandSize
        val paddingH = 20
        val paddingW = 20
        var left = -distW + paddingW
        var top = distH + paddingH
        var right = distW * (bandSize - 1) - paddingW
        var bottom = distH * (bandSize - 1) - paddingH

        for((i, band) in bandList.withIndex()) {
            band.layout(left, top, right, bottom)
            left += distW
            right += distW

            when(i) {
            /*0 -> band.setBackgroundColor(Color.GREEN)
            1 -> band.setBackgroundColor(Color.YELLOW)
            2 -> band.setBackgroundColor(Color.RED)
            3 -> band.setBackgroundColor(Color.WHITE)
            4 -> band.setBackgroundColor(Color.BLUE)*/
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    inner class BandView @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): SeekBar(context, attrs, defStyle, defStyleRes) {

        private val VERTICAL = 270f
        private val WIDTH = 50
        private val MAX = 50

        init {
            rotation = VERTICAL
            max = MAX

            val thumb = ShapeDrawable(OvalShape())

            thumb.intrinsicHeight = 80
            thumb.intrinsicWidth = 30
            setThumb(thumb)
            progress = 1
            visibility = View.VISIBLE
            setBackgroundColor(Color.BLUE)

            val lp = LayoutParams(50, 100)
            layoutParams = lp

            setPadding(20, 20, 0, 20)
        }
    }

}