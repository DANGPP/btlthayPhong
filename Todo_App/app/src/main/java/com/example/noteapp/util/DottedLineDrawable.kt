package com.example.noteapp.util

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class DottedLineDrawable(
    private val color: Int,
    private val strokeWidth: Float = 1f,
    private val dashWidth: Float = 10f,
    private val dashGap: Float = 5f,
    private val horizontal: Boolean = true
) : Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        this.strokeWidth = this@DottedLineDrawable.strokeWidth
        color = this@DottedLineDrawable.color
        pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
    }

    private val path = Path()

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        path.reset()
        
        if (horizontal) {
            val y = bounds.height() / 2f
            path.moveTo(bounds.left.toFloat(), y)
            path.lineTo(bounds.right.toFloat(), y)
        } else {
            val x = bounds.width() / 2f
            path.moveTo(x, bounds.top.toFloat())
            path.lineTo(x, bounds.bottom.toFloat())
        }
        
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
