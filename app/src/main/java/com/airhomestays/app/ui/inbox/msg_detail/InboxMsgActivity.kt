package com.airhomestays.app.ui.inbox.msg_detail

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
import com.airhomestays.app.databinding.ActivityInboxMessagesBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.inbox.InboxNavigator
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.slideDown
import com.airhomestays.app.util.slideUp
import com.airhomestays.app.vo.InboxMsgInitData
import org.jetbrains.annotations.Nullable
import javax.inject.Inject

class InboxMsgActivity : BaseActivity<ActivityInboxMessagesBinding, InboxMsgViewModel>(),
    InboxNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityInboxMessagesBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_inbox_messages
    override val viewModel: InboxMsgViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(InboxMsgViewModel::class.java)

    private lateinit var adapter: InboxMsgAdapter

    companion object {
        @JvmStatic
        fun openInboxMsgDetailsActivity(activity: Activity, inboxMsgData: InboxMsgInitData) {
            val intent = Intent(activity, InboxMsgActivity::class.java)
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
            mBinding.tvNewMsgPill.slideDown()
            viewModel.notificationRefresh()
        }
    }

    private fun initView() {
        mBinding.tvInboxSend.onClick {
            checkNetwork { viewModel.sendMsg() }
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
                adapter = InboxMsgAdapter(
                    it.hostId,
                    it.hostPicture,
                    it.guestPicture,
                    it.senderID,
                    it.receiverID,
                    clickCallback = { },
                    retryCallback = { viewModel.notificationRetry() })
                mBinding.rvInboxDetails.adapter = adapter
                viewModel.getInboxMsg()
            }
        })

        viewModel.posts.observe(this, Observer<PagedList<GetThreadsQuery.ThreadItem>> { pagedList ->
            pagedList?.let {
                adapter.submitList(it)
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
                if (gotNewMsg) mBinding.tvNewMsgPill.slideUp()
            }
        })
    }


    override fun openListingDetails() {

    }

    override fun moveToBackScreen() {
        val intent = Intent()
        setResult(23, intent)
    }

    override fun onRetry() {
        viewModel.sendMsg()
    }

    override fun onBackPressed() {
        viewModel.dataManager.deleteMessage()
        super.onBackPressed()
    }

    override fun addMessage(text: @Nullable SendMessageMutation.Results) {
        val list = viewModel.posts.value
        /*list?.add(0, GetThreadsQuery.ThreadItem(
                "",23,32, 23, text, "233", "23", "322", "2332", "23"
        ))
        viewModel.posts.value?.add(0, GetThreadsQuery.ThreadItem(
         "",23,32, 23, text, "233", "23", "322", "2332", "23"
        ))*/
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun hideTopView(msg: String) {

    }

    override fun openBillingActivity() {

    }
}