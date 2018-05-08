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
    private var bandConnectorShadow: BandConnectorShadow? = null

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
        bandConnectorShadow = BandConnectorShadow(context)
        addView(bandConnectorShadow)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Warning! In order to set bands vertically,
        // the one to manipulate is not the height, but the WIDTH!
        // Because the band is ROTATED 270 degrees!
        setBandsVertically(w, h)
        bandConnectorLayout?.layout(0, 0, w, h)
        bandConnectorLayout?.connect(bandList)

        // Layout and draw band connector shadow
        bandConnectorShadow?.layout(0, 0, w, h)
        bandConnectorShadow?.draw(bandList)
    }

    private fun setBandsVertically(width: Int, height: Int) {
        // TODO This method only works on 3 bands! Make it flexible by using other numbers!
        val distW = width / bandSize
        val distH = height / bandSize
        var left = (-height / 2 + (distW / 2))
        var right = (height / 2 + (distW / 2))

        for(band in bandList) {
            // Calculate height of band
            val forceBandHeight = PixelUtil.dpToPx(context, 20f)
            val top = ((height / 2 - forceBandHeight)).toInt()
            val bottom = ((height / 2 + forceBandHeight)).toInt()
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
            verticalGridPath.moveTo(currentX, height.toFloat())
            verticalGridPath.lineTo(currentX, 0f)
            canvas?.drawPath(verticalGridPath, gridLinePaint)

            currentX += distW
        }

        // Set horizontal line
        val horizontalGridPath = Path()
        horizontalGridPath.moveTo(0f, height.toFloat() / 2)
        horizontalGridPath.lineTo(width.toFloat(), height.toFloat() / 2)
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
        bandConnectorShadow?.draw(bandList)
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
                val offset = bounds.width() / 2
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

    inner class BandConnectorShadow @JvmOverloads constructor(
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
                val offset = bounds.width() / 2
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