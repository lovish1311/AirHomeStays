package com.airhomestays.app.ui.host.hostInbox

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airhomestays.app.BR
import com.airhomestays.app.GetAllThreadsQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.data.remote.paging.Status
import com.airhomestays.app.databinding.HostFragmentInboxBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.hostInbox.host_msg_detail.HostNewInboxMsgActivity
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderListingDetailsSectionHeader
import com.airhomestays.app.viewholderLoader
import com.airhomestays.app.vo.InboxMsgInitData
import javax.inject.Inject

class HostInboxFragment : BaseFragment<HostFragmentInboxBinding, HostInboxViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostFragmentInboxBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_inbox
    override val viewModel: HostInboxViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(HostInboxViewModel::class.java)

    private var pagingController = InboxListController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.shimmer.visible()
        mBinding.shimmer.startShimmer()
        CustomSpringAnimation.spring(mBinding.rvInbox)
    }

    private fun subscribeToLiveData() {
        viewModel.loadInbox()
            .observe(this, Observer<PagedList<GetAllThreadsQuery.Result>> { pagedList ->
                pagedList?.let {
                    if (mBinding.rvInbox.adapter == pagingController.adapter) {
                        pagingController.submitList(it)
                    } else {
                        mBinding.rvInbox.adapter = pagingController.adapter
                        pagingController.submitList(it)
                    }
                }
            })

        viewModel.networkState.observe(this, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.rlInboxNomessagePlaceholder.visible()
                        pagingController.isLoading = false
                        mBinding.shimmer.gone()
                        mBinding.tvInbox.visible()
                    }

                    NetworkState.LOADING -> {
                        mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = true
                    }

                    NetworkState.EXPIRED -> {
                        openSessionExpire("HostInboxFragment")
                    }

                    NetworkState.LOADED -> {
                        mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = false
                        mBinding.rvInbox.visible()
                        mBinding.ltLoadingView.gone()
                        mBinding.shimmer.gone()
                        mBinding.tvInbox.gone()
                    }

                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            it.msg?.let { error ->
                                viewModel.handleException(error)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        mBinding.rvInbox.adapter = null
        super.onDestroyView()
    }

    fun onRefresh() {
        if (::mViewModelFactory.isInitialized && isAdded && activity != null) {
            mBinding.rvInbox.gone()
            mBinding.ltLoadingView.visible()
            mBinding.shimmer.visible()
            mBinding.tvInbox.visible()
            viewModel.inboxRefresh()
        }
    }

    override fun onRetry() {
        viewModel.getInboxList()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class InboxListController : PagedListEpoxyController<GetAllThreadsQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(
            currentPosition: Int,
            item: GetAllThreadsQuery.Result?
        ): EpoxyModel<*> {
            try {
                return HostInboxListEpoxyGroup(currentPosition, item!!, clickListener = {

                    HostNewInboxMsgActivity.openInboxMsgDetailsActivity(
                        baseActivity!!, InboxMsgInitData(
                            threadId = item.threadItem?.threadId?:0,
                            guestId = item.guest!!,
                            guestName = item.guestProfile?.firstName!!,
                            guestPicture = item.guestProfile?.picture,
                            hostId = item.host!!,
                            hostName = item.hostProfile?.firstName!!,
                            hostPicture = item.hostProfile?.picture,
                            senderID = item.guestProfile?.profileId!!,
                            receiverID = item.hostProfile?.profileId!!,
                            listID = item.listId
                        )
                    )
                }, avatarClick = {
                    UserProfileActivity.openProfileActivity(
                        context!!,
                        it.guestProfile?.profileId!!
                    )
                })
            } catch (e: Exception) {
                e.printStackTrace()
                showError()
            }
            return HostInboxListEpoxyGroup(
                currentPosition,
                item!!,
                clickListener = {},
                avatarClick = {
                })
        }

        override fun addModels(models: List<EpoxyModel<*>>) {
            try {
                viewholderListingDetailsSectionHeader {
                    id("header")
                    header(baseActivity!!.resources.getString(R.string.inbox))
                }
                super.addModels(models)
                if (isLoading) {
                    viewholderLoader {
                        id("loading")
                        isLoading(this@InboxListController.isLoading)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError()
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