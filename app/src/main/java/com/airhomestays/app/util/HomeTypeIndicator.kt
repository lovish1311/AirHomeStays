package com.airhomestays.app.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HomeTypeIndicator(context: Context,
                        private val indicatorHeight: Int,
                        private val indicatorColor: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private var currentVisibleItemPosition: Int = RecyclerView.NO_POSITION

    init {
        paint.color = indicatorColor
        paint.style = Paint.Style.FILL
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val layoutManager = parent.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val itemCount = parent.adapter?.itemCount ?: 0
            if (itemCount == 0) return

            // Find the first visible item position
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val findlastVisibleItemPosition=layoutManager.findLastVisibleItemPosition()
            // Check if the visible item has changed
            if (firstVisibleItemPosition != currentVisibleItemPosition) {

                currentVisibleItemPosition = firstVisibleItemPosition
            }

            // Get the view of the currently visible item
            val visibleView = layoutManager.findViewByPosition(currentVisibleItemPosition)

            if (visibleView != null) {
                val left = visibleView.left
                val right = visibleView.right
                val indicatorTop = visibleView.bottom
                val indicatorBottom = visibleView.bottom + indicatorHeight
                c.drawRect(left.toFloat(), indicatorTop.toFloat(), right.toFloat(), indicatorBottom.toFloat(), paint)
            }
        }
    }
}









