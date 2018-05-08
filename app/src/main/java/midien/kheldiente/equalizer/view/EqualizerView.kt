package midien.kheldiente.equalizer.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
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

    private val BAND_NAME_HEIGHT = 30f

    private var bandSize = 0
    private var progressDrawable = 0
    private var thumb = 0

    private val bandList: ArrayList<BandView> = ArrayList(bandSize)
    private var bandNameLayout: BandNameLayout? = null
    private var bandConnectorLayout: BandConnectorLayout? = null
    private var bandConnectorShadowView: BandConnectorShadowView? = null

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
        // call onDraw() to setup grid lines
        setWillNotDraw(false)

        // Add default (3) band views
        for(index in 1..bandSize) {
            val bv = BandView(context)
            bv.progressDrawable = resources.getDrawable(progressDrawable, null)
            bv.thumb = resources.getDrawable(thumb, null)
            bv.tag = index
            bv.setOnSeekBarChangeListener(this)
            // Add to list for reference
            bandList.add(bv)
            // Add to display
            addView(bv)
        }

        bandConnectorLayout = BandConnectorLayout(context)
        addView(bandConnectorLayout)

        bandConnectorShadowView = BandConnectorShadowView(context)
        addView(bandConnectorShadowView)

        bandNameLayout = BandNameLayout(context)
        addView(bandNameLayout)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Warning! In order to set bands vertically,
        // the one to manipulate is not the height, but the WIDTH!
        // Because the band is ROTATED 270 degrees!
        setBandsVertically(w, h)

        bandConnectorLayout?.layout(0, 0, w, h)
        bandConnectorLayout?.connect(bandList)

        // Layout and draw band connector shadow
        bandConnectorShadowView?.layout(0, 0, w, h)
        bandConnectorShadowView?.draw(bandList)

        bandNameLayout?.layout(0,
            (h - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT * 2)).toInt(),
            w,
            h)
    }

    private fun setBandsVertically(width: Int, height: Int) {
        val distW = width / bandSize
        val distH = height / bandSize
        var left = (-height / 2 + (distW / 2)) + PixelUtil.dpToPx(context, BAND_NAME_HEIGHT).toInt()
        var right = (height / 2 + (distW / 2)) - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT).toInt()

        for(band in bandList) {
            // Calculate height of band
            val forceBandHeight = PixelUtil.dpToPx(context, 20f)
            val top = ((height / 2 - forceBandHeight)).toInt() - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT).toInt()
            val bottom = ((height / 2 + forceBandHeight)).toInt() - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT).toInt()
            // Draw bandview ALWAYS IN FRONT!
            band.bringToFront()
            band.layout(left, top, right, bottom)
            left += distW
            right += distW
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    private fun setGridLines(canvas: Canvas?) {
        // Init paint
        val gridLinePaint = Paint()
        gridLinePaint.color = Color.WHITE
        gridLinePaint.alpha = 50
        gridLinePaint.strokeWidth = PixelUtil.dpToPx(context, 1f)
        gridLinePaint.style = Paint.Style.STROKE

        // Set vertical grid lines
        val distW = width.toFloat() / bandSize
        var currentX = distW - (distW / 2)
        for (i in 1..bandSize) {
            val verticalGridPath = Path()
            verticalGridPath.moveTo(currentX, height.toFloat() - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT))
            verticalGridPath.lineTo(currentX, 0f)
            canvas?.drawPath(verticalGridPath, gridLinePaint)

            currentX += distW
        }

        // Set horizontal line
        val horizontalGridPath = Path()
        horizontalGridPath.moveTo(0f, (height.toFloat() / 2) - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT))
        horizontalGridPath.lineTo(width.toFloat(), (height.toFloat() / 2) - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT))
        canvas?.drawPath(horizontalGridPath, gridLinePaint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setGridLines(canvas)
    }

    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
        // Redraw band connector path
        bandConnectorLayout?.connect(bandList)
        // Redraw band connector shadow
        bandConnectorShadowView?.draw(bandList)
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

        private val TAG = BandConnectorLayout::class.java.simpleName

        private var pathPaint: Paint = Paint()
        private var path: Path = Path()

        init {
            // Init paint
            pathPaint.color = Color.GREEN
            pathPaint.strokeWidth = PixelUtil.dpToPx(context, 5f)
            pathPaint.style = Paint.Style.STROKE
        }

        fun connect(bandList: ArrayList<BandView>) {
            // Redraw path
            path.reset()
            for((index, band) in bandList.withIndex()) {
                val bounds = band.thumb.bounds
                val offset = (bounds.width() / 2) - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT * 2).toInt()
                var distW = width.toFloat() / bandList.size
                val x = bounds.centerX().toFloat()
                var y: Float
                if(index == 0) {
                    y = (distW / 2) * (index + 1)
                    path.moveTo(y, (height.toFloat() - x) + offset)
                } else {
                    y = ((distW) * (index + 1)) - (distW / 2)
                    path.lineTo(y, (height.toFloat() - x) + offset)
                }
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            canvas?.drawPath(path, pathPaint)
        }
    }

    inner class BandNameLayout @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): View(context, attrs, defStyle, defStyleRes) {

        private val textPaint = Paint()

        init {
            setBackgroundColor(Color.BLACK)

            // Init paint
            textPaint.color = Color.WHITE
            textPaint.alpha = 200
            textPaint.textSize = PixelUtil.dpToPx(context, 15f)
            textPaint.textAlign = Paint.Align.CENTER
            drawBandNames()
        }

        private fun drawBandNames() {
            invalidate()
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            val size = 5
            val distW = width / size // 6 for now
            var centerX = (distW / 2)
            for(i in 1..size) {
                canvas?.drawText("450Hz", centerX.toFloat(), (height / 2).toFloat(), textPaint)
                centerX += distW
            }
        }

    }

    inner class BandConnectorShadowView @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyle: Int = 0,
            defStyleRes: Int = 0
    ): View(context, attrs, defStyle, defStyleRes) {

        private var pathPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var path: Path = Path()

        fun draw(bandList: ArrayList<BandView>) {
            // Redraw path
            path.reset()

            val startX = (width.toFloat() / bandList.size) / 2
            val startY = height.toFloat()
            path.moveTo(startX, startY)

            for((index, band) in bandList.withIndex()) {
                val bounds = band.thumb.bounds
                val offset = (bounds.width() / 2) - PixelUtil.dpToPx(context, BAND_NAME_HEIGHT * 2).toInt()
                var distW = width.toFloat() / bandList.size
                val x = bounds.centerX().toFloat()
                var y: Float
                if(index == 0) {
                    y = (distW / 2) * (index + 1)
                    path.lineTo(y, (height.toFloat() - x) + offset)
                } else {
                    y = ((distW) * (index + 1)) - (distW / 2)
                    path.lineTo(y, (height.toFloat() - x) + offset)
                }
            }

            val endX = width.toFloat() - startX
            val endY = startY
            path.lineTo(endX, endY)

            path.close()
            // Note: Shader should be set AFTER paths are set
            pathPaint.shader = LinearGradient(0f,0f,0f, height.toFloat(), Color.GREEN, Color.TRANSPARENT, Shader.TileMode.CLAMP)

            invalidate()
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            canvas?.drawPath(path, pathPaint)
        }
    }


}