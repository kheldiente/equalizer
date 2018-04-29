package midien.kheldiente.equalizer.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlin.collections.ArrayList
import midien.kheldiente.equalizer.R
import midien.kheldiente.equalizer.util.PixelUtil

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
    private var bandConnectorLayout: BandConnectorLayout? = null

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

        bandConnectorLayout = BandConnectorLayout(context)
        addView(bandConnectorLayout)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Warning! In order to set bands vertically,
        // the one to manipulate is not the height, but the WIDTH!
        // Because the band is ROTATED 270 degrees!
        setBandsVertically(w, h)
        bandConnectorLayout?.layout(0, 0, w, h)
        bandConnectorLayout?.connect(bandList)
    }

    private fun setBandsVertically(width: Int, height: Int) {
        // TODO This method only works on 3 bands! Make it flexible by using other numbers!
        val distW = width / bandSize
        val distH = height / bandSize
        val paddingH = 0
        val paddingW = 0
        var left = (-height / 2 + (distW / 2)) + paddingH
        var right = (height / 2 + (distW / 2)) - paddingH

        for((index, band) in bandList.withIndex()) {
            // Calculate height of band
            val forceBandHeight = PixelUtil.dpToPx(context, 20f)
            val top = ((height / 2 - forceBandHeight) + paddingH).toInt()
            val bottom = ((height / 2 + forceBandHeight) - paddingH).toInt()

            band.layout(left, top, right, bottom)
            left += distW
            right += distW
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
        // Redraw band connector path
        bandConnectorLayout?.connect(bandList)
    }

    override fun onStartTrackingTouch(seekbar: SeekBar?) {}

    override fun onStopTrackingTouch(seekbar: SeekBar?) {}

    inner class BandView @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): SeekBar(context, attrs, defStyle, defStyleRes) {

        private val VERTICAL = 270f // default: 270f
        private val MAX = 50
        private val PROGRESS = 25

        init {
            rotation = VERTICAL
            max = MAX
            // Init progress
            progress = PROGRESS
         }

    }

    inner class BandConnectorLayout @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): View(context, attrs, defStyle, defStyleRes) {

        private val TAG = BandConnectorLayout::class.java.simpleName;

        private var paint: Paint = Paint()
        private var path: Path = Path()

        init {
            // Init paint
            paint.color = Color.GREEN
            paint.strokeWidth = PixelUtil.dpToPx(context, 10f)
            paint.style = Paint.Style.STROKE
        }

        fun connect(bandList: ArrayList<BandView>) {
            // Redraw path
            path.reset()
            for((index, band) in bandList.withIndex()) {
                val tag = band.tag
                val bounds = band.thumb.bounds
                val x = bounds.centerX().toFloat()
                var y = (band.width.toFloat() / bandList.size) * (index + 1)
                if(index == 0) {
                    path.moveTo(50f, height.toFloat())
                    path.lineTo(50f, x)
                    break
                } else {
                    y -= 100f
                    path.lineTo(y, height.toFloat() - x)
                }
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            canvas?.drawPath(path, paint)
        }
    }


}