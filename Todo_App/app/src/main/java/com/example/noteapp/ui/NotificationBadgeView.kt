package com.example.noteapp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.noteapp.R

/**
 * Custom view to show notification badge with count
 */
class NotificationBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var count: Int = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = ContextCompat.getColor(context, android.R.color.holo_red_light)
        paint.style = Paint.Style.FILL

        textPaint.color = ContextCompat.getColor(context, android.R.color.white)
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun setCount(count: Int) {
        this.count = count
        visibility = if (count > 0) VISIBLE else GONE
        invalidate()
    }

    fun getCount(): Int = count

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (count <= 0) return

        val radius = width / 2f
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)

        val text = if (count > 99) "99+" else count.toString()
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(text, x, y, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = if (count > 9) 60 else 48
        setMeasuredDimension(size, size)
    }
}
