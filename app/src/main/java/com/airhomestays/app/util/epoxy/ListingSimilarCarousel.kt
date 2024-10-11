package com.airhomestays.app.util.epoxy

import android.content.Context
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView
import com.airhomestays.app.util.DecoratorExplore
import com.airhomestays.app.util.Dectorator


@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListingSimilarCarousel(context: Context) : Carousel(context) {

    init {
        addItemDecoration(DecoratorExplore(context.resources, context, this))
        CustomSpringHorizontalAnimation.spring(this)
    }

    @NonNull
    override fun createLayoutManager(): androidx.recyclerview.widget.RecyclerView.LayoutManager {
        return LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
    }
}