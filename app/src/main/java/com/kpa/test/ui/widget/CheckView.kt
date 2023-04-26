package com.kpa.test.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class CheckView : View {
    private var mCountable = false
    private var mChecked = false
    private var mCheckedNum = 0
    private var mStrokePaint: Paint? = null
    private var mBackgroundPaint: Paint? = null
    private var mTextPaint: TextPaint? = null
    private var mShadowPaint: Paint? = null
    private val mCheckDrawable: Drawable? = null
    private var mDensity = 0f
    private var mCheckRect: Rect? = null
    private var mEnabled = true

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // fixed size 48dp x 48dp
        val sizeSpec = MeasureSpec.makeMeasureSpec((SIZE * mDensity).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(sizeSpec, sizeSpec)
    }

    private fun init(context: Context) {
        mDensity = context.resources.displayMetrics.density
        mStrokePaint = Paint()
        mStrokePaint!!.isAntiAlias = true
        mStrokePaint!!.style = Paint.Style.STROKE
        mStrokePaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        mStrokePaint!!.strokeWidth = STROKE_WIDTH * mDensity
        //        TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.item_checkCircle_borderColor});
//        int defaultColor = ResourcesCompat.getColor(
//                getResources(), R.color.zhihu_item_checkCircle_borderColor,
//                getContext().getTheme());
//        int color = ta.getColor(0, defaultColor);
//        ta.recycle();
//        mStrokePaint.setColor(color);

//        mCheckDrawable = ResourcesCompat.getDrawable(context.getResources(),
//                R.drawable.ic_check_white_18dp, context.getTheme());
    }

    fun setChecked(checked: Boolean) {
        check(!mCountable) { "CheckView is countable, call setCheckedNum() instead." }
        mChecked = checked
        invalidate()
    }

    fun setCountable(countable: Boolean) {
        mCountable = countable
    }

    fun setCheckedNum(checkedNum: Int) {
        check(mCountable) { "CheckView is not countable, call setChecked() instead." }
        require(!(checkedNum != UNCHECKED && checkedNum <= 0)) { "checked num can't be negative." }
        mCheckedNum = checkedNum
        invalidate()
    }

    override fun setEnabled(enabled: Boolean) {
        if (mEnabled != enabled) {
            mEnabled = enabled
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw outer and inner shadow
        initShadowPaint()
        canvas.drawCircle(
            SIZE.toFloat() * mDensity / 2,
            SIZE.toFloat() * mDensity / 2,
            (STROKE_RADIUS + STROKE_WIDTH / 2 + SHADOW_WIDTH) * mDensity,
            mShadowPaint!!,
        )

        // draw white stroke
        canvas.drawCircle(
            SIZE.toFloat() * mDensity / 2,
            SIZE.toFloat() * mDensity / 2,
            STROKE_RADIUS * mDensity,
            mStrokePaint!!,
        )

        // draw content
        if (mCountable) {
            if (mCheckedNum != UNCHECKED) {
                initBackgroundPaint()
                canvas.drawCircle(
                    SIZE.toFloat() * mDensity / 2,
                    SIZE.toFloat() * mDensity / 2,
                    BG_RADIUS * mDensity,
                    mBackgroundPaint!!,
                )
                initTextPaint()
                val text = mCheckedNum.toString()
                val baseX = (canvas.width - mTextPaint!!.measureText(text)).toInt() / 2
                val baseY =
                    (canvas.height - mTextPaint!!.descent() - mTextPaint!!.ascent()).toInt() / 2
                canvas.drawText(text, baseX.toFloat(), baseY.toFloat(), mTextPaint!!)
            }
        } else {
            if (mChecked) {
                initBackgroundPaint()
                canvas.drawCircle(
                    SIZE.toFloat() * mDensity / 2,
                    SIZE.toFloat() * mDensity / 2,
                    BG_RADIUS * mDensity,
                    mBackgroundPaint!!,
                )
                mCheckDrawable!!.bounds = checkRect
                mCheckDrawable.draw(canvas)
            }
        }

        // enable hint
        alpha = if (mEnabled) 1.0f else 0.5f
    }

    private fun initShadowPaint() {
        if (mShadowPaint == null) {
            mShadowPaint = Paint()
            mShadowPaint!!.isAntiAlias = true
            // all in dp
            val outerRadius = STROKE_RADIUS + STROKE_WIDTH / 2
            val innerRadius = outerRadius - STROKE_WIDTH
            val gradientRadius = outerRadius + SHADOW_WIDTH
            val stop0 = (innerRadius - SHADOW_WIDTH) / gradientRadius
            val stop1 = innerRadius / gradientRadius
            val stop2 = outerRadius / gradientRadius
            val stop3 = 1.0f
            mShadowPaint!!.shader = RadialGradient(
                SIZE.toFloat() * mDensity / 2,
                SIZE.toFloat() * mDensity / 2,
                gradientRadius * mDensity,
                intArrayOf(
                    Color.parseColor("#00000000"),
                    Color.parseColor("#0D000000"),
                    Color.parseColor("#0D000000"),
                    Color.parseColor("#00000000"),
                ),
                floatArrayOf(stop0, stop1, stop2, stop3),
                Shader.TileMode.CLAMP,
            )
        }
    }

    private fun initBackgroundPaint() {
        if (mBackgroundPaint == null) {
            mBackgroundPaint = Paint()
            mBackgroundPaint!!.isAntiAlias = true
            mBackgroundPaint!!.style = Paint.Style.FILL
            //            TypedArray ta = getContext().getTheme()
//                    .obtainStyledAttributes(new int[]{R.attr.item_checkCircle_backgroundColor});
//            int defaultColor = ResourcesCompat.getColor(
//                    getResources(), R.color.zhihu_item_checkCircle_backgroundColor,
//                    getContext().getTheme());
//            int color = ta.getColor(0, defaultColor);
//            ta.recycle();
//            mBackgroundPaint.setColor(color);
        }
    }

    private fun initTextPaint() {
        if (mTextPaint == null) {
            mTextPaint = TextPaint()
            mTextPaint!!.isAntiAlias = true
            mTextPaint!!.color = Color.WHITE
            mTextPaint!!.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            mTextPaint!!.textSize = 12.0f * mDensity
        }
    }

    // rect for drawing checked number or mark
    private val checkRect: Rect
        private get() {
            if (mCheckRect == null) {
                val rectPadding = (SIZE * mDensity / 2 - CONTENT_SIZE * mDensity / 2).toInt()
                mCheckRect = Rect(
                    rectPadding,
                    rectPadding,
                    (SIZE * mDensity - rectPadding).toInt(),
                    (SIZE * mDensity - rectPadding).toInt(),
                )
            }
            return mCheckRect!!
        }

    companion object {
        const val UNCHECKED = Int.MIN_VALUE
        private const val STROKE_WIDTH = 3.0f // dp
        private const val SHADOW_WIDTH = 6.0f // dp
        private const val SIZE = 48 // dp
        private const val STROKE_RADIUS = 11.5f // dp
        private const val BG_RADIUS = 11.0f // dp
        private const val CONTENT_SIZE = 16 // dp
    }
}
