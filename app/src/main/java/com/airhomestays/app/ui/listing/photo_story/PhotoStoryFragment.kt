package com.airhomestays.app.ui.listing.photo_story

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.Carousel.setDefaultGlobalSnapHelperFactory
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderListingDetailsPhotoStoryBindingModel_
import com.airhomestays.app.databinding.FragmentListingPhotoStoryBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.ui.listing.share.ShareActivity
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.photoStoryCarousel
import javax.inject.Inject

class PhotoStoryFragment : BaseFragment<FragmentListingPhotoStoryBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_photo_story
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentListingPhotoStoryBinding
    private var id: Int? = null
    private var title: String? = null
    private var img: String? = null
    private var photoPosition = 0
    private lateinit var epoxyVisibilityTracker: EpoxyVisibilityTracker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(mBinding.rlListingPhotos)
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressedDispatcher?.onBackPressed() }
        mBinding.ivShareListingDetails.onClick {
            try {
                ShareActivity.openShareIntent(context!!, id!!, title!!, viewModel.carouselUrl.value!!,
                    Utils.TransitionAnim(requireContext(),"slideup"))
            } catch (e: KotlinNullPointerException) { }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.initialValue.observe(viewLifecycleOwner, Observer { photo -> photo?.let {
            try {
                id = it.id
                title = it.title
                img = viewModel.carouselUrl.value //it.listPhotos()!![0].name()!!
                initEpoxy(it.photo)
            } catch (e: KotlinNullPointerException) {

            }
        } })

        viewModel.carouselPosition.observe(viewLifecycleOwner, Observer {
            it?.let { position ->
                if (photoPosition != position) {
                    photoPosition = position
                    mBinding.rlListingPhotos.requestModelBuild()
                }
            }
        })
    }

    private fun initEpoxy(photos: ArrayList<String>) {
        mBinding.rlListingPhotos.withModels {
            photoStoryCarousel {
                id("photoCarousel")
                padding(Carousel.Padding(0, 0, 0, 0, 10))
                setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                    override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                        return PagerSnapHelper()
                    }
                })
                models(buildMostViwedModel(photos))
                onBind { _, view, _ ->
                    (view as RecyclerView).scrollToPosition(photoPosition)
                }
            }
        }
    }

    private fun buildMostViwedModel(it: ArrayList<String>) : List<EpoxyModel<*>> {
        val photoStoryModels = java.util.ArrayList<EpoxyModel<*>>()
        it.forEachIndexed { index, item ->
            photoStoryModels.add(ViewholderListingDetailsPhotoStoryBindingModel_()
                    .id("$index - $item")
                    .url(item)
                    .count(index.plus(1).toString() + "/" + it.size)
                    .onVisibilityChanged { model, _, _, percentVisibleWidth, heightVisible, _ ->
                        if (percentVisibleWidth == 100F) {
                            viewModel?.setCarouselCurrentPhoto(model.url())
                        }
                    }
            )
        }
        return photoStoryModels
    }

    override fun onDestroy() {
        epoxyVisibilityTracker.detach(mBinding.rlListingPhotos)
        super.onDestroy()
    }

    override fun onRetry() {

    }
    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }
}