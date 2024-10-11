@file:JvmName("ExtensionsUtils1")

package com.airhomestays.app.util

import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airhomestays.app.util.epoxy.*

/** Easily add models to an EpoxyRecyclerView, the same way you would in a buildModels method of SampleController. */
fun EpoxyRecyclerView.withModels(buildModelsCallback: EpoxyController.() -> Unit) {
    setControllerAndBuildModels(object : EpoxyController() {
        override fun buildModels() {
            try {
                buildModelsCallback()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

    })
}

inline fun EpoxyController.photoStoryCarousel(modelInitializer: ListingPhotoStoryCarouselModelBuilder.() -> Unit) {
    ListingPhotoStoryCarouselModel_().apply {
        modelInitializer()
    }.addTo(this)
}

inline fun <T> ListingPhotoStoryCarouselModelBuilder.withModelsFrom(
        items: List<T>,
        modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}

inline fun EpoxyController.exploreGridCarousel(modelInitializer: ExploreGridCarouselModelBuilder.() -> Unit) {
    ExploreGridCarouselModel_().apply {
        modelInitializer()
    }.addTo(this)
}

inline fun EpoxyController.addListingPhotoCarousel(modelInitializer: AddListingPhotoCarouselModelBuilder.() -> Unit) {
    AddListingPhotoCarouselModel_().apply {
        modelInitializer()
    }.addTo(this)
}

inline fun EpoxyController.listingPhotosCarousel(modelInitializer: ListingPhotosCarouselModelBuilder.() -> Unit) {
    ListingPhotosCarouselModel_().apply {
        modelInitializer()
    }.addTo(this)
}

inline fun EpoxyController.listingSimilarCarousel(modelInitializer: ListingSimilarCarouselModelBuilder.() -> Unit) {
    ListingSimilarCarouselModel_().apply {
        modelInitializer()
    }.addTo(this)
}

inline fun <T> ExploreGridCarouselModelBuilder.withModelsFrom(
        items: List<T>,
        modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}

inline fun <T> ListingPhotosCarouselModelBuilder.withModelsFrom(
        items: List<T>,
        modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}

inline fun <T> ListingSimilarCarouselModelBuilder.withModelsFrom(
        items: List<T>,
        modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}