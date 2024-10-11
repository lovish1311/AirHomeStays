package com.airhomestays.app.util.epoxy

import android.util.Log
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airhomestays.app.ui.saved.SavedDetailFragment

class CustomSpringAnimation {
    companion object{
        private const val SCROLL_ROTATION_MAGNITUDE = 0.25f

        /** The magnitude of rotation while the list is over-scrolled. */
        private const val OVERSCROLL_ROTATION_MAGNITUDE = -10

        /** The magnitude of translation distance while the list is over-scrolled. */
        private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f

        /** The magnitude of translation distance when the list reaches the edge on fling. */
        private const val FLING_TRANSLATION_MAGNITUDE = 0.5f

        fun spring(recyclerView: RecyclerView){
            val springAnim = recyclerView.let { img ->
                // Setting up a spring animation to animate the view’s translationY property with the final
                // spring position at 0.
                SpringAnimation(img, DynamicAnimation.TRANSLATION_Y, 0f).setSpring(
                        SpringForce()
                                .setFinalPosition(0f)
                                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                                .setStiffness(SpringForce.STIFFNESS_VERY_LOW)
                )
            }

            recyclerView.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                    return object : EdgeEffect(recyclerView.context) {

                        override fun onPull(deltaDistance: Float) {
                            super.onPull(deltaDistance)
                            handlePull(deltaDistance)
                        }

                        override fun onPull(deltaDistance: Float, displacement: Float) {
                            super.onPull(deltaDistance, displacement)
                            handlePull(deltaDistance)
                        }

                        private fun handlePull(deltaDistance: Float) {
                            // This is called on every touch event while the list is scrolled with a finger.
                            // We simply update the view properties without animation.
                            Log.e("TAG direction",direction.toString())
                            val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                            val rotationDelta = sign * deltaDistance * OVERSCROLL_ROTATION_MAGNITUDE
                            val translationYDelta = sign * recyclerView.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
                            recyclerView.translationY += translationYDelta

                        }

                        override fun onRelease() {
                            super.onRelease()
                            springAnim.animateToFinalPosition(0f)
                        }

                        override fun onAbsorb(velocity: Int) {
                            super.onAbsorb(velocity)
                            val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                            val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
                            springAnim.setStartVelocity(translationVelocity)
                                    .start()
                        }
                    }
                }
            }
        }
    }
}