package com.airhomestays.app.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.airhomestays.app.R

class Dectorator(val resources: Resources, val context: Context, recyclerView: androidx.recyclerview.widget.RecyclerView) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    companion object {

        private val DEFAULT_DOT_COUNT = 5
        private val DEFAULT_FADING_DOT_COUNT = 1
        private val DEFAULT_DOT_RADIUS_DP = 4
        private val DEFAULT_SELECTED_DOT_RADIUS_DP = 5.5f
        // Measured outside of first dot to outside of next dot: O<->O
        private val DEFAULT_DOT_SEPARATION_DISTANCE_DP = 10

        private fun dpToPx(dp: Float, resources: Resources): Int =
                (dp * ((resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT).toFloat())).toInt()

    }

    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var internalRecyclerScrollListener: InternalRecyclerScrollListener? = null
    private val interpolator = DecelerateInterpolator()

    private var dotCount = DEFAULT_DOT_COUNT
    private var fadingDotCount = DEFAULT_FADING_DOT_COUNT
    private var selectedDotRadiusPx = dpToPx(DEFAULT_SELECTED_DOT_RADIUS_DP, resources)
    private var dotRadiusPx = dpToPx(DEFAULT_DOT_RADIUS_DP.toFloat(), resources)
    private var dotSeparationDistancePx = dpToPx(DEFAULT_DOT_SEPARATION_DISTANCE_DP.toFloat(), resources)
    private var supportRtl = true

    @ColorInt
    private var dotColor: Int = ContextCompat.getColor(this.context, R.color.indicator_unselected)
    @ColorInt
    private var selectedDotColor: Int = ContextCompat.getColor(this.context, R.color
            .colorPrimary)
    private val selectedDotPaint = Paint()
    private val dotPaint = Paint()

    /**
     * The current pager position. Used to draw the selected dot if different size/color.
     */
    private var selectedItemPosition: Int = 0

    /**
     * A temporary value used to reflect changes/transition from one selected item to the next.
     */
    private var intermediateSelectedItemPosition: Int = 0

    /**
     * The scroll percentage of the viewpager or recyclerview.
     * Used for moving the dots/scaling the fading dots.
     */
    private var offsetPercent: Float = 0f

    init {
        this.recyclerView = recyclerView
        dotCount = DEFAULT_DOT_COUNT
        fadingDotCount = DEFAULT_FADING_DOT_COUNT
        dotRadiusPx = dotRadiusPx
        selectedDotRadiusPx = selectedDotRadiusPx
        dotColor = dotColor
        selectedDotColor = selectedDotColor
        dotSeparationDistancePx = dotSeparationDistancePx
        supportRtl = true

        selectedDotPaint.style = Paint.Style.FILL
        selectedDotPaint.color = selectedDotColor
        selectedDotPaint.isAntiAlias = true
        dotPaint.style = Paint.Style.FILL
        dotPaint.color = dotColor
        dotPaint.isAntiAlias = true
        internalRecyclerScrollListener = InternalRecyclerScrollListener()

        this.recyclerView?.addOnScrollListener(internalRecyclerScrollListener as androidx.recyclerview.widget.RecyclerView.OnScrollListener)

    }

    override fun onDrawOver(c: Canvas, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val indicatorPosY = parent.height - 60f// mIndicatorHeight ;

        if (parent.adapter!!.itemCount>1) {
            for (i in 0 until parent.adapter!!.itemCount) {
                val xCoordinate = getDotXCoordinate(i)
                val normalizedX = this@Dectorator.recyclerView!!.width / 2 + xCoordinate
                c.drawCircle(normalizedX.toFloat(), indicatorPosY, getRadius(xCoordinate), getPaint(xCoordinate))
            }
        }
    }


    /**
     * Get the x coordinate for a dot based on the position in the pager.
     */
    private fun getDotXCoordinate(pagerPosition: Int): Float =
            (pagerPosition - intermediateSelectedItemPosition) * (dotSeparationDistancePx + 2 * dotRadiusPx) + ((dotSeparationDistancePx + 2 * dotRadiusPx) * offsetPercent)

    /**
     * Get the y coordinate for a dot.
     *
     * The bottom of the view is y = 0 and a dot is drawn from the center, so therefore
     * the y coordinate is simply the radius.
     */
    private fun getDotYCoordinate(): Int {
        return selectedDotRadiusPx
    }


    /**
     * Calculates a dot's radius based on x position.
     *
     * If the x position is within 1 dot's length of x = 0, it is the currently selected dot.
     *
     * If the x position is within a threshold (half the width of the number of non fading dots),
     * it is a normal sized dot.
     *
     * If the x position is outside of the above threshold, it is a fading dot or not visible. The
     * radius is calculated based on a interpolator percentage of how far the
     * viewpager/recyclerview has scrolled.
     */
    private fun getRadius(xCoordinate: Float): Float {
        val xAbs = Math.abs(xCoordinate)
        // Get the x coordinate where dots begin showing as fading dots (x coordinates > half of width of all large dots)
        val largeDotThreshold = dotCount.toFloat() / 2 * (dotSeparationDistancePx + 2 * dotRadiusPx)
        return when {
            xAbs < (dotSeparationDistancePx + 2 * dotRadiusPx) / 2 -> selectedDotRadiusPx.toFloat()
            xAbs <= largeDotThreshold -> dotRadiusPx.toFloat()
            else -> {
                // Determine how close the dot is to the edge of the view for scaling the size of the dot
                val percentTowardsEdge = (xAbs - largeDotThreshold) / (getCalculatedWidth() / 2.01f - largeDotThreshold)
                interpolator.getInterpolation(1 - percentTowardsEdge) * dotRadiusPx
            }
        }
    }

    /**
     * Returns the dot's color based on x coordinate, similar to {@link #getRadius(Float)}.
     *
     * If the x position is within 1 dot's length of x = 0, it is the currently selected dot.
     *
     * All other dots will be the normal specified dot color.
     */
    private fun getPaint(xCoordinate: Float): Paint = when {
        Math.abs(xCoordinate) < (dotSeparationDistancePx + 2 * dotRadiusPx) / 2 -> selectedDotPaint
        else -> dotPaint
    }

    /**
     * Get the calculated with of the view.
     *
     * Calculated by the total number of visible dots (normal & fading).
     *
     * TODO: Add support for padding.
     */
    private fun getCalculatedWidth(): Int {
        val maxNumVisibleDots = dotCount + 2 * fadingDotCount
        return (maxNumVisibleDots - 1) * (dotSeparationDistancePx + 2 * dotRadiusPx) + 2 * dotRadiusPx
    }

    internal inner class InternalRecyclerScrollListener : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

        /**
         * The previous most visible child page in the RecyclerView.
         *
         * Used to differentiate between the current most visible child page to correctly determine
         * the currently selected item and percentage scrolled.
         */
        private var previousMostVisibleChild: View? = null

        /**
         * Determine based on the percentage a child viewholder's view is visible what position
         * is the currently selected.
         *
         * Use this percentage to also calculate the offsetPercentage
         * used to scale dots.
         */
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {

            val view = getMostVisibleChild()

            if (view != null) {
                setIntermediateSelectedItemPosition(view)
                offsetPercent = view.left.toFloat() / view.measuredWidth
            }

            with(recyclerView?.layoutManager as androidx.recyclerview.widget.LinearLayoutManager) {
                val visibleItemPosition = if (dx >= 0) findLastVisibleItemPosition() else findFirstVisibleItemPosition()

                if (previousMostVisibleChild !== findViewByPosition(visibleItemPosition)) {
                    selectedItemPosition = intermediateSelectedItemPosition
                }
            }

            previousMostVisibleChild = view
            this@Dectorator.recyclerView?.invalidate()
        }

        /**
         * Returns the currently most visible viewholder view in the Recyclerview.
         *
         * The most visible view is determined based on percentage of the view visible. This is
         * calculated below in calculatePercentVisible().
         */
        private fun getMostVisibleChild(): View? {
            var mostVisibleChild: View? = null
            var mostVisibleChildPercent = 0f
            for (i in recyclerView?.layoutManager?.childCount!! - 1 downTo 0) {
                val child = recyclerView?.layoutManager?.getChildAt(i)
                if (child != null) {
                    val percentVisible = calculatePercentVisible(child)
                    if (percentVisible >= mostVisibleChildPercent) {
                        mostVisibleChildPercent = percentVisible
                        mostVisibleChild = child
                    }
                }
            }

            return mostVisibleChild
        }

        private fun calculatePercentVisible(child: View): Float {
            val left = child.left
            val right = child.right
            val width = child.width

            return when {
                left < 0 -> right / width.toFloat()
                right > this@Dectorator.recyclerView!!.width -> (this@Dectorator.recyclerView!!.width - left) / width.toFloat()
                else -> 1f
            }
        }

        private fun setIntermediateSelectedItemPosition(mostVisibleChild: View?) {
            with(recyclerView?.findContainingViewHolder(mostVisibleChild!!)?.adapterPosition!!) {
                intermediateSelectedItemPosition = this
            }
        }
    }



}