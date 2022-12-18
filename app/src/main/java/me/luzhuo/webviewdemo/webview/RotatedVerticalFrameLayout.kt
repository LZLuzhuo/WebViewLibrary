package me.luzhuo.webviewdemo.webview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * 将FrameLayout旋转90°
 */
class RotatedVerticalFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        rotation = 90f
        translationX = (height - width) / 2f
        translationY = (width - height) / 2f

        layoutParams.height = width
        layoutParams.width = height
        requestLayout()
    }
}