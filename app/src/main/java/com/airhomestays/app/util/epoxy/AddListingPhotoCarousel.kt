package com.airhomestays.app.util.epoxy

import android.content.Context
import androidx.annotation.NonNull
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)

class AddListingPhotoCarousel(context: Context) : Carousel(context) {

    @NonNull
    override fun createLayoutManager(): androidx.recyclerview.widget.RecyclerView.LayoutManager {
        return GridLayoutManager(context, SPAN_COUNT, LinearLayoutManager.VERTICAL, false)
    }

    companion object {
        private val SPAN_COUNT = 2
    }
}