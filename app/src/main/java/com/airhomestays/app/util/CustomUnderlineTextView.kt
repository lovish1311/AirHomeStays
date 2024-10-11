package com.airhomestays.app.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.airhomestays.app.R

class CustomUnderlineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    @RequiresApi(Build.VERSION_CODES.M)
    private val underlinePaint = Paint().apply {
        color = context.getColor(R.color.black)
        isAntiAlias = true
    }

    private var showUnderlines = true

    fun showUnderlines(show: Boolean) {
        showUnderlines = show
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showUnderlines) {
            drawUnderlines(canvas)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawUnderlines(canvas: Canvas) {
        val lineCount = lineCount
        val descent = paint.descent()

        for (i in 0 until lineCount) {
            val lineStartX = layout.getLineLeft(i) + paddingStart
            val lineEndX = layout.getLineRight(i) - paddingEnd
            val lineBottom = layout.getLineBottom(i).toFloat() + 8

            canvas.drawRect(lineStartX, lineBottom - descent, lineEndX, lineBottom - descent - 2f, underlinePaint)
        }
    }
}