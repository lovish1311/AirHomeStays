package com.airhomestays.app.ui.saved

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airhomestays.app.BR
import com.airhomestays.app.GetAllWishListGroupQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderSavedListItemsBindingModel_
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.data.remote.paging.Status
import com.airhomestays.app.databinding.FragmentSavedBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderListingDetailsSectionHeader
import com.airhomestays.app.viewholderLoader
import timber.log.Timber
import javax.inject.Inject

class NewSavedFragment : BaseFragment<FragmentSavedBinding, SavedViewModel>(), SavedNavigator {
    override fun reloadExplore() {

    }

    override fun showEmptyMessageGroup() {

    }

    override fun moveUpScreen() {

    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentSavedBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_saved
    override val viewModel: SavedViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(SavedViewModel::class.java)
    private var pagingController = WishListGroupController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.tvStartExplore.onClick {
            (baseActivity as HomeActivity).setExplore()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadwishListGroup().observe(
            viewLifecycleOwner,
            Observer<PagedList<GetAllWishListGroupQuery.Result>> { pagedList ->
                pagedList?.let {
                    if (mBinding.rvSaved.adapter == pagingController.adapter) {
                        pagingController.submitList(it)
                    } else {
                        mBinding.rvSaved.adapter = pagingController.adapter
                        pagingController.submitList(it)
                    }
                }
            })

        viewModel.wishListGroupNetworkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.rlSaveNoListPlaceholder.visible()
                        pagingController.isLoading = false
                        mBinding.ltLoadingView.gone()
                        mBinding.root.visible()
                    }

                    NetworkState.LOADING -> {
                        mBinding.rlSaveNoListPlaceholder.gone()
                        if (viewModel.isRefreshing) {
                            mBinding.root.gone()
                            mBinding.ltLoadingView.visible()
                        }
                    }

                    NetworkState.EXPIRED -> {
                        openSessionExpire("NewSavedFrag")
                    }

                    NetworkState.LOADED -> {
                        if (viewModel.isRefreshing) {
                            viewModel.isRefreshing = false
                        }
                        mBinding.rlSaveNoListPlaceholder.gone()
                        pagingController.isLoading = false
                        mBinding.ltLoadingView.gone()
                        mBinding.root.visible()
                    }

                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            mBinding.ltLoadingView.gone()
                            mBinding.root.visible()
                            it.msg?.let { thr ->
                                viewModel.handleException(thr)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })

    }

    override fun onDestroyView() {
        mBinding.rvSaved.adapter = null
        super.onDestroyView()
    }



    override fun onRetry() {
        if (::mViewModelFactory.isInitialized) {
            viewModel.wishListGroupRetry()
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .add(mBinding.flSavedFragment.id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    fun refresh() {
        mBinding.root.gone()
        mBinding.ltLoadingView.visible()
        childFragmentManager.popBackStack()
        viewModel.wishListGroupRefresh()
    }

    inner class WishListGroupController :
        PagedListEpoxyController<GetAllWishListGroupQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(
            currentPosition: Int,
            item: GetAllWishListGroupQuery.Result?
        ): EpoxyModel<*> {
            try {
                return ViewholderSavedListItemsBindingModel_()
                    .id("savedList - ${item?.id}")
                    .url(item?.wishListCover?.listData?.listPhotoName)
                    .name(item?.name)
                    .wishListCount(item?.wishListCount)
                    .clickListener(View.OnClickListener {
                        try {
                            openFragment(
                                SavedDetailFragment.newInstance(
                                    item?.id!!,
                                    item.name!!,
                                    item.wishListCount!!,
                                    viewModel.wishlistType!!
                                ), "SavedDetails"
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showError()
                        }
                    })
            } catch (e: Exception) {
                Timber.e(e, "CRASH")
            }
            return ViewholderSavedListItemsBindingModel_()
        }

        override fun addModels(models: List<EpoxyModel<*>>) {

            try {
                viewholderListingDetailsSectionHeader {
                    id("header")
                    header(baseActivity!!.resources.getString(R.string.savedd))
                }
                super.addModels(models)
                if (isLoading) {
                    viewholderLoader {
                        id("loading")
                        isLoading(this@WishListGroupController.isLoading)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "CRASH")
            }
        }

        init {
            isDebugLoggingEnabled = true
        }

        override fun onExceptionSwallowed(exception: RuntimeException) {
            throw exception
        }
    }

}