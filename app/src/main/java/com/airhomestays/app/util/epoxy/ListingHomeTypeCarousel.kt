package com.airhomestays.app.util.epoxy

import android.content.Context
import android.graphics.Color
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView
import com.airhomestays.app.util.DecoratorExplore
import com.airhomestays.app.util.HomeTypeIndicator

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListingHomeTypeCarousel(context: Context) : Carousel(context) {

    init {
        addItemDecoration(HomeTypeIndicator(context, 50, Color.BLACK))
        CustomSpringHorizontalAnimation.spring(this)
    }

    @NonNull
    override fun createLayoutManager(): androidx.recyclerview.widget.RecyclerView.LayoutManager {
        return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }
}