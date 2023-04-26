package com.kpa.test.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * @author: kpa
 * @date: 2023/4/19
 * @description:
 */
open class SquareFrameLayout : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
