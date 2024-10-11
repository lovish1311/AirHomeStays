package com.airhomestays.app.ui.host.hostInbox.host_msg_detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.GetThreadsQuery
import com.airhomestays.app.R
import com.airhomestays.app.SendMessageMutation
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.databinding.HostActivityInboxMessagesBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.inbox.InboxNavigator
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.InboxMsgInitData
import javax.inject.Inject

class HostInboxMsgActivity :
    BaseActivity<HostActivityInboxMessagesBinding, HostInboxMsgViewModel>(), InboxNavigator {

    override fun addMessage(text: SendMessageMutation.Results) {

    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostActivityInboxMessagesBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_inbox_messages
    override val viewModel: HostInboxMsgViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(HostInboxMsgViewModel::class.java)

    private lateinit var adapter: HostInboxMsgAdapter

    companion object {
        @JvmStatic
        fun openInboxMsgDetailsActivity(activity: Activity, inboxMsgData: InboxMsgInitData) {
            val intent = Intent(activity, HostInboxMsgActivity::class.java)
            intent.putExtra("inboxInitData", inboxMsgData)
            activity.startActivityForResult(intent, 53)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        topView = mBinding.rlInboxDetailSendMsg
        viewModel.setInitialData(intent)
        initView()
        subscribeToLiveData()
        mBinding.tvNewMsgPill.invisible()
        mBinding.tvNewMsgPill.onClick {
            mBinding.tvNewMsgPill.gone()
            viewModel.notificationRefresh()
        }
    }

    private fun initView() {
        val from = intent?.getStringExtra("from")
        from?.let {
            if (it == "fcm") {
                viewModel.clearHttp()
            }
        }
        mBinding.tvInboxSend.onClick {
            checkNetwork {
                viewModel.sendMsg()
            }
        }
        mBinding.toolbarInbox.ivCameraToolbar.gone()
        mBinding.toolbarInbox.ivNavigateup.onClick { finish() }
        mBinding.toolbarInbox.tvToolbarHeading.text = resources.getString(R.string.message)
        val layout = LinearLayoutManager(this)
        layout.stackFromEnd = true
        mBinding.rvInboxDetails.layoutManager = layout
    }

    private fun subscribeToLiveData() {
        viewModel.inboxInitData.observe(this, Observer { initData ->
            initData?.let { it ->
                mBinding.shimmer.gone()
                adapter = HostInboxMsgAdapter(
                    it.hostId,
                    it.hostPicture,
                    it.guestPicture,
                    it.senderID,
                    it.receiverID,
                    clickCallback = { },
                    retryCallback = {
                        viewModel.notificationRetry()
                    }
                )
                mBinding.rvInboxDetails.adapter = adapter
                viewModel.getInboxMsg()
            }
        })

        viewModel.posts.observe(this, Observer<PagedList<GetThreadsQuery.ThreadItem>> { pagedList ->
            pagedList?.let {
                adapter.submitList(it)
                readMessage()
            }
        })

        viewModel.networkState.observe(this, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.shimmer.gone()
                    }

                    NetworkState.LOADING -> {
                        adapter.setNetworkState(it)
                    }

                    NetworkState.LOADED -> {
                        adapter.setNetworkState(it)
                        mBinding.shimmer.gone()
                    }

                    NetworkState.EXPIRED -> {
                        openSessionExpire("HostInboxMsgAct")
                    }

                    NetworkState.FAILED -> {
                        adapter.setNetworkState(it)
                    }

                    else -> {
                        adapter.setNetworkState(it)
                    }
                }
            }
        })

        viewModel.isNewMessage.observe(this, Observer {
            it?.let { gotNewMsg ->
                if (gotNewMsg) mBinding.tvNewMsgPill.visible()
            }
        })
    }

    private fun readMessage() {
        try {
            if (viewModel.posts.value!!.size <= 10) {
                viewModel.readMessage()
                viewModel.newMsg()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun hideTopView(msg: String) {

    }

    override fun moveToBackScreen() {
        val intent = Intent()
        setResult(23, intent)
    }

    override fun onRetry() {
        viewModel.sendMsg()
    }

    override fun openBillingActivity() {
    }

    override fun openListingDetails() {

    }
}