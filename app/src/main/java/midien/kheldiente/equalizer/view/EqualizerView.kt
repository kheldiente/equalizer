package midien.kheldiente.equalizer.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlin.collections.ArrayList
import midien.kheldiente.equalizer.R
import kotlin.properties.Delegates


class EqualizerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
): ViewGroup(context, attrs, defStyle, defStyleRes), SeekBar.OnSeekBarChangeListener {

    private val TAG = EqualizerView::class.java.simpleName

    private var bandSize = 0
    private var progressDrawable = 0
    private var thumb = 0

    private val bandList: ArrayList<BandView> = ArrayList(bandSize)

    init {
        attrs?.let {
            val typedArray = context.theme.obtainStyledAttributes(attrs,
                    R.styleable.EqualizerView,
                    0,
                    0)
            // Get set attr value for bands
            bandSize = typedArray.getInteger(R.styleable.EqualizerView_bands, 3)
            progressDrawable = typedArray.getResourceId(R.styleable.EqualizerView_progressDrawable, R.drawable.seekbar_style)
            thumb = typedArray.getResourceId(R.styleable.EqualizerView_thumb, R.drawable.custom_thumb)
            typedArray.recycle()

            initDefaults()
        }
    }

    private fun initDefaults() {
        // Add default (3) band views
        for(index in 1..bandSize) {
            val bv = BandView(context)
            bv.progressDrawable = resources.getDrawable(progressDrawable)
            bv.thumb = resources.getDrawable(thumb)
            bv.tag = index
            bv.setOnSeekBarChangeListener(this)
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

        for(band in bandList) {
            band.layout(left, top, right, bottom)
            left += distW
            right += distW
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
        val bv = seekbar as BandView
        val tag = bv.tag
        val thumbX = bv.getThumbX()
        val middle = bv.getMiddle()
        Log.d(TAG, String.format("tag: %s, thumbX: %s, middle: %s", tag, thumbX, middle))
    }

    override fun onStartTrackingTouch(seekbar: SeekBar?) {}

    override fun onStopTrackingTouch(seekbar: SeekBar?) {}

    inner class BandView @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): SeekBar(context, attrs, defStyle, defStyleRes) {

        private val VERTICAL = 270f
        private val MAX = 50
        private val PROGRESS = 25

        private var thumbX: Double = 0.0
        private var middle: Double = 0.0

        init {
            rotation = VERTICAL
            max = MAX
            // Init progress
            progress = PROGRESS
         }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            thumbX = ((progress.toDouble() / max.toDouble()) * width.toDouble())
            middle = (height / 2.0)
        }

        fun getThumbX(): Double {
           return thumbX
        }

        fun getMiddle(): Double {
            return middle
        }

    }

    inner class BandConnectorView @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): View(context, attrs, defStyle, defStyleRes) {

        private var paint: Paint = Paint()
        private var path: Path = Path()

        init {
            // Init paint
            paint.color = Color.GREEN
            paint.strokeWidth = 10f
            paint.style = Paint.Style.STROKE
        }

        fun connect(startX: Float, startY: Float, endX: Float, endY: Float) {
            path.moveTo(startX, startY)
            path.lineTo(endX, endY)
        }
    }


}