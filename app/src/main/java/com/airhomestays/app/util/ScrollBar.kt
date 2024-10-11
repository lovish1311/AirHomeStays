package com.airhomestays.app.util

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airhomestays.app.R
import com.airhomestays.app.ui.explore.ExploreFragment


class ScrollBar : FrameLayout {
     var mScrollBar: View? = null
     var mScrollBarLay: View? = null
    private var mRecycleView: RecyclerView? = null
    private var isUserScrolling = false
    private var targetPosition = 0


    var colors: List<Int>? = null
        private set


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        colors = listOf(Color.GRAY,Color.BLACK)
        mScrollBarLay = LayoutInflater.from(context).inflate(R.layout.scroll_view, null)
        mScrollBar = mScrollBarLay!!.findViewById<View>(R.id.scrollbar)
        addView(mScrollBarLay)
    }

    fun setRecycleView(recycleView: RecyclerView?) {
        mRecycleView = recycleView
        mRecycleView!!.addOnScrollListener(scrollListener)
    }

    fun scroll(dx: Int) {
        if (mRecycleView == null) {
            return
        }
        var range = 0
        val temp = mRecycleView!!.computeHorizontalScrollRange()
        if (temp > range) {
            range = temp
        }
        val offset = mRecycleView!!.computeHorizontalScrollOffset()
        val extent = mRecycleView!!.computeHorizontalScrollExtent()
        val proportion = (offset * 1.0 / (range - extent)).toFloat()
        val transMaxRange = (mScrollBarLay!!.width - mScrollBar!!.width).toFloat()
        mScrollBar!!.translationX = transMaxRange * proportion
        getRecyclerViewItem(dx)
    }

    var tempPosition = 0f
    var count = 0
    var item = 1.03f

    fun getRecyclerViewItem(dx: Int) {
        if (mRecycleView == null) {
            return
        }
        val itemCount = mRecycleView!!.adapter?.itemCount ?: 0


        val scrollbarPercentage = mScrollBar!!.translationX / (mScrollBarLay!!.width - mScrollBar!!.width)


        tempPosition = scrollbarPercentage * itemCount

        targetPosition = (scrollbarPercentage * itemCount).coerceIn(0F, (itemCount - 1).toFloat()).toInt()



        val interpolator = FastOutSlowInInterpolator()

        val current: View? = if (targetPosition==mRecycleView!!.layoutManager!!.itemCount) {
            mRecycleView!!.layoutManager!!.findViewByPosition(targetPosition-1)
        } else {
            mRecycleView!!.layoutManager!!.findViewByPosition(targetPosition)
        }

        val targetChildWidth = current!!.width

        val duration = 400L
        val steps = 350

        val stepDuration = duration / steps
        val stepWidth = (targetChildWidth - mScrollBar!!.width) / steps.toFloat()
        var currentWidth = mScrollBar!!.width.toFloat()


        for (i in 1..steps) {
            handler.postDelayed({
                currentWidth += stepWidth
                mScrollBar!!.layoutParams.width = currentWidth.toInt()
                mScrollBar!!.requestLayout()
            }, (stepDuration * i * interpolator.getInterpolation(i.toFloat() / steps)).toLong())
        }





    }


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isUserScrolling = false

            } else if(newState == RecyclerView.SCROLL_STATE_SETTLING) {
                isUserScrolling = false

            } else if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layout = mRecycleView!!.layoutManager!! as LinearLayoutManager
                layout.smoothScrollToPosition(mRecycleView,RecyclerView.State(),targetPosition)
                layout.scrollToPositionWithOffset(targetPosition, mScrollBar!!.translationX.toInt())

            }
        }
    }



    companion object {
        private const val TAG = "ScrollBar"
    }
}