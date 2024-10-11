package com.airhomestays.app.ui.saved

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderSavedListCarouselBindingModel_
import com.airhomestays.app.databinding.FragmentSavedBottomsheetBinding
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseBottomSheet
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.ui.saved.createlist.CreateListActivity
import com.airhomestays.app.util.epoxy.listingPopularCarousel
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderCenterTextPlaceholder
import com.airhomestays.app.vo.SavedList
import javax.inject.Inject


class SavedBotomSheet : BaseBottomSheet<FragmentSavedBottomsheetBinding, SavedViewModel>(),
    SavedNavigator {
    override fun reloadExplore() {

    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_saved_bottomsheet
    override val viewModel: SavedViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(SavedViewModel::class.java)
    lateinit var mBinding: FragmentSavedBottomsheetBinding
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory
    private var listId = 0
    private var isSimilar = false
    private var listImage = ""
    private var listGroupCount = 0
    private var savedList = ArrayList<SavedList>()

    companion object {
        private const val LISTID = "param1"
        private const val ISSIMILAR = "param2"
        private const val LISTIMAGE = "param3"
        private const val LISTGROUPCOUNT = "param4"

        @JvmStatic
        fun newInstance(
            type: Int,
            listImage: String,
            isSimilar: Boolean = false,
            groupCount: Int? = 0
        ) =
            SavedBotomSheet().apply {
                arguments = Bundle().apply {
                    putInt(LISTID, type)
                    groupCount?.let { putInt(LISTGROUPCOUNT, it) }
                    putString(LISTIMAGE, listImage)
                    putBoolean(ISSIMILAR, isSimilar)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        arguments?.let {
            listId = it.getInt(LISTID)
            listGroupCount = it.getInt(LISTGROUPCOUNT)
            isSimilar = it.getBoolean(ISSIMILAR)
            listImage = it.getString(LISTIMAGE, "")
        }
        viewModel.setListDetails(listId, listImage, listGroupCount)
        snapHelperFactory = object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                return PagerSnapHelper()
            }
        }
        mBinding.ivAdd.onClick {
            val intent = Intent(context, CreateListActivity::class.java)
            intent.putExtra("listID", listId)
            intent.putExtra("name", "")
            startActivityForResult(intent, 35)
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.loadWishListGroup().observe(this) { it ->
            it?.let {
                savedList = ArrayList(it)
                if (mBinding.rvSavedWishlist.adapter != null) {
                    mBinding.rvSavedWishlist.requestModelBuild()
                } else {
                    setup()
                }
            }
        }

        viewModel.isLoadingInProgess.observe(this) {
            isCancelable = it != 1

        }

    }

    private fun setup() {
        mBinding.rvSavedWishlist.withModels {
            if (savedList.isNotEmpty()) {
                listingPopularCarousel {
                    id("SimilarCarousel11")
                    Carousel.setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                    models(buildModel())
                }
            } else {
                viewholderCenterTextPlaceholder {
                    id("noresult")
                    header(resources.getString(R.string.no_wishlist_groups_added_please_create_group_by_click_add_icon))
                    large(false)
                    switcher(false)
                }
            }
        }
    }

    private fun buildModel(): MutableList<out EpoxyModel<*>> {
        val models = ArrayList<EpoxyModel<*>>()
        for (i in 0 until savedList.size) {
            models.add(ViewholderSavedListCarouselBindingModel_()
                .id("savedList - ${savedList[i].id}")
                .url(savedList[i].img)
                .name(savedList[i].title)
                .wishListStatus(savedList[i].isWishList)
                .wishListCount(savedList[i].wishListCount)
                .progress(savedList[i].progress)
                .isRetry(savedList[i].isRetry)
                .onRetryClick(View.OnClickListener {
                    savedList[i].progress = AuthViewModel.LottieProgress.LOADING
                    savedList[i].isRetry = savedList[i].id.toString()
                    mBinding.rvSavedWishlist.requestModelBuild()
                    viewModel.retryCalled =
                        "create-$listId-${savedList[i].id}-${savedList[i].isWishList.not()}"
                    viewModel.createWishList(
                        listId,
                        savedList[i].id,
                        savedList[i].isWishList.not()
                    )
                })
                .heartClickListener(View.OnClickListener {
                    savedList[i].progress = AuthViewModel.LottieProgress.LOADING
                    mBinding.rvSavedWishlist.requestModelBuild()
                    viewModel.retryCalled =
                        "create-$listId-${savedList[i].id}-${savedList[i].isWishList.not()}"
                    viewModel.createWishList(
                        listId,
                        savedList[i].id,
                        savedList[i].isWishList.not()
                    )
                })
                .clickListener(View.OnClickListener {

                })
            )
        }
        return models
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 2) {
            mBinding.rvSavedWishlist.clear()
            viewModel.getAllWishListGroup()
        }
    }

    override fun onRetry() {
        viewModel.loadWishListGroup()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (viewModel.getIsWishListAdded()) {
            if (baseActivity is HomeActivity) {
                if (viewModel.listGroupCount.value!! > 0) {
                    (baseActivity as HomeActivity).refreshExploreItems(
                        viewModel.listId.value,
                        true,
                        viewModel.listGroupCount.value!!
                    )
                } else {
                    if(viewModel.duplicateid >0){
                        (baseActivity as HomeActivity).refreshExploreItems(
                            viewModel.listId.value,
                            true,
                            viewModel.listGroupCount.value!!
                        )
                    }else{
                        (baseActivity as HomeActivity).refreshExploreItems(
                            viewModel.listId.value,
                            false,
                            viewModel.listGroupCount.value!!
                        )
                    }
                }

            } else if (baseActivity is ListingDetails) {
                if (isSimilar) {
                    (baseActivity as ListingDetails).loadSimilarDetails()
                } else {
                    (baseActivity as ListingDetails).loadListingDetails()
                }
            } else if (baseActivity is ReservationActivity) {
                (baseActivity as ReservationActivity).loadListingDetails()
            }
        }
        viewModel.duplicateid=0
    }


    override fun moveUpScreen() {
        dismiss()
    }

    override fun showEmptyMessageGroup() {

    }
}