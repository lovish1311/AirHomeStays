package com.airhomestays.app.ui.host.hostInbox.host_msg_detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.GetThreadsQuery
import com.airhomestays.app.R
import com.airhomestays.app.SendMessageMutation
import com.airhomestays.app.databinding.HostActivityInboxMessagesBinding
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.cancellation.CancellationActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.inbox.InboxNavigator
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.InboxMsgInitData
import com.airhomestays.app.vo.PreApproved
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class HostNewInboxMsgActivity :
    BaseActivity<HostActivityInboxMessagesBinding, HostInboxMsgViewModel>(), InboxNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostActivityInboxMessagesBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_inbox_messages
    override val viewModel: HostInboxMsgViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(HostInboxMsgViewModel::class.java)

    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private var paginationAdapter: PaginationAdapter? = null
    private var progressBar: ProgressBar? = null
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var layoutManager: LinearLayoutManager? = null
    private var isLoadedAll = false
    private lateinit var disposable: Disposable

    var guestName = ""
    var guestProfileID = 0


    var handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null

    var preAdded: Boolean = false
    var approved: Boolean = false

    var from = ""

    companion object {
        @JvmStatic
        fun openInboxMsgDetailsActivity(activity: Activity, inboxMsgData: InboxMsgInitData) {
            val intent = Intent(activity, HostNewInboxMsgActivity::class.java)
            intent.putExtra("inboxInitData", inboxMsgData)
            activity.startActivityForResult(intent, 53)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.shimmer.startShimmer()
        topView = mBinding.rlInboxDetailSendMsg
        try {
            from = intent?.getStringExtra("from")!!
            from?.let {
                if (it == "fcm") {
                    if (viewModel.loginStatus == 0) {
                        startActivity(Intent(this, AuthActivity::class.java))
                        this.finish()
                    }
                }
            }
        } catch (e: Exception) {
            from = ""
        }
        viewModel.setInitialData(intent)
        initView()
        subscribeToLiveData()
        setUpLoadMoreListener()
        subscribeForData()
        Timber.d("subscribe1")
        mBinding.tvNewMsgPill.invisible()
        mBinding.tvNewMsgPill.onClick {
            mBinding.tvNewMsgPill.gone()
            pageNumber = 1
            compositeDisposable.remove(disposable)
            subscribeForData()
            Timber.d("subscribe2")
            paginationAdapter?.removeItems()
        }
    }


    private fun initView() {

        mBinding.toolbarInbox.ivCameraToolbar.gone()
        mBinding.toolbarInbox.ivNavigateup.onClick {
            if (from != null && from == "fcm") {
                val intent = Intent(this, HostHomeActivity::class.java)
                intent.putExtra("from", "fcm")
                startActivity(intent,Utils.TransitionAnim(this,"right").toBundle())
                finish()
            } else {
                handler.removeCallbacks(runnable ?: Runnable { })
                finish()
            }
        }
        mBinding.toolbarInbox.tvToolbarHeading.text = resources.getString(R.string.message)
        layoutManager = LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).orientation = RecyclerView.VERTICAL
        mBinding.rvInboxDetails.layoutManager = layoutManager
        (layoutManager as LinearLayoutManager).reverseLayout = true
        mBinding.tvInboxSend.onClick {
            checkNetwork {
                mBinding.tvInboxSend.disable()
                if (!viewModel.msg.get().equals("")) {
                    viewModel.sendMsg()
                }
            }
        }
        mBinding.rvInboxDetails.adapter?.setHasStableIds(true)

    }
fun refreshThread(){
    pageNumber = 1
    compositeDisposable.remove(disposable)
    subscribeForData()
    Timber.d("subscribe2")
    paginationAdapter?.removeItems()
}

    private fun subscribeToLiveData() {
        viewModel.inboxInitData.observe(this, Observer { initData ->
            initData?.let { it ->
                paginationAdapter = PaginationAdapter(
                    it.hostId,
                    it.guestPicture,
                    it.hostPicture,
                    it.receiverID,
                    it.senderID
                )
                mBinding.rvInboxDetails.adapter = paginationAdapter
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
            viewModel.readMessage()
            viewModel.newMsg()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpLoadMoreListener() {
        mBinding.rvInboxDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int, dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = layoutManager!!.itemCount
                lastVisibleItem = layoutManager!!.findLastVisibleItemPosition()
                if (!isLoadedAll && !loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    pageNumber++
                    paginator.onNext(pageNumber)
                    loading = true
                }
            }
        })
    }

    private fun subscribeForData() {
        disposable = paginator
            .onBackpressureDrop()
            .doOnNext { page ->
                loading = true
                progressBar?.visibility = View.VISIBLE
            }
            .concatMapSingle { page ->
                viewModel.getInboxMsg1(page)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { }
                    .doFinally { }
                    .doOnSuccess {
                        if (page == 1) {
                            readMessage()
                        }
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                if (items!!.data!!.getThreads!!.status == 200) {
                    enableSendButton()
                    if (items.data!!.getThreads!!.results!!.threadItems!!.isNotEmpty()) {
                        if (items.data!!.getThreads!!.results!!.threadItems!!.size < 10) {
                            isLoadedAll = true
                        }
                        try {
                            guestName =
                                items.data!!.getThreads!!.results!!.guestProfile!!.firstName!!
                            guestProfileID =
                                items.data!!.getThreads!!.results!!.guestProfile!!.profileId!!

                            getPreApproved(
                                items.data!!.getThreads!!.results!!.threadItemForType!!
                            )

                        } catch (e: Exception) {

                        }
                        paginationAdapter!!.addItems(
                            items.data!!.getThreads!!.results!!.threadItems!!
                        )
                        paginationAdapter!!.notifyDataSetChanged()
                    } else {
                        isLoadedAll = true
                    }
                } else if (items.data!!.getThreads!!.status == 400) {
                    mBinding.inlError.root.visible()
                } else if (items.data!!.getThreads!!.status == 500) {
                    openSessionExpire("")
                }
                loading = false
            }, { t ->
                if (pageNumber == 1) {
                    viewModel.isRetry.set(2)
                    viewModel.handleException(t)
                } else {
                    viewModel.handleException(t, true)
                }
            })
        compositeDisposable.add(disposable)
        paginator.onNext(pageNumber)
    }

    private fun enableSendButton() {
        mBinding.tvInboxSend.isClickable = true
        mBinding.etInput.isClickable = true
        mBinding.tvInboxSend.enable()
        mBinding.etInput.enable()
    }

    fun getPreApproved(threadItems: GetThreadsQuery.ThreadItemForType) {
        try {
            var cont = ""
            var reserId = 0
            if (threadItems.reservationId != null) {
                reserId = threadItems.reservationId!!
            }
            if (threadItems.content != null) {
                cont = threadItems.content!!
            }
            val start = Utils.getGmtDate(threadItems.startDate!!.toLong())
            val end = Utils.getGmtDate(threadItems.endDate!!.toLong())
            viewModel.preApproved.value = PreApproved(
                threadItems.threadId!!,
                threadItems.type!!,
                createdAt = threadItems.createdAt!!.toLong(),
                endDate = end,
                startDate = start,
                personCapacity = threadItems.personCapacity!!.toInt(),
                visitors = threadItems.visitors!!.toInt(),
                infants = threadItems.infants!!.toInt(),
                pets = threadItems.pets!!.toInt(),
                content = cont,
                reservationID = reserId
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (viewModel.preApproved.value?.id != 0) {
            viewModel.preApprovalVisible.set(true)
            if (viewModel.preApproved.value!!.type.equals("requestToBook")) {
                val remaining = Utils.getMilliSec(viewModel.preApproved.value!!.createdAt)
                if (remaining < 1440) {
                    setRequestBook()
                } else {
                    viewModel.preApprovalVisible.set(false)
                }
            } else if (viewModel.preApproved.value!!.type.equals("inquiry")) {
                setUpPreApproval()
            } else if (viewModel.preApproved.value!!.type.equals("preApproved")) {
                setRequestSent()
            } else if (viewModel.preApproved.value!!.type.equals("approved")
            ) {
                approvedVisibility()
            }else if (viewModel.preApproved.value!!.type.equals(
                    "instantBooking")) {
                setCancelBooking()
                }else if (viewModel.preApproved.value!!.type.equals("cancelledByHost") || viewModel.preApproved.value!!.type.equals(
                    "cancelledByGuest"
                )
            ) {
                bookingCancelled()
            } else if (viewModel.preApproved.value!!.type.equals("declined")) {
                declinedBooking()
            } else if (viewModel.preApproved.value!!.type.equals("completed")) {
                completedReserv()
            } else {
                viewModel.preApprovalVisible.set(false)
            }
        } else {
            viewModel.preApprovalVisible.set(false)
            preAdded = true
        }


    }

    fun setUpPreApproval() {
        with(mBinding) {
            preHeaderText = getString(R.string.pre_approved_header, guestName)
            presubText = getString(R.string.pre_approved_sub_text, guestName)
            presubVisible = false // visible
            declineVisible = false
            prebuttonText = getString(R.string.pre_approval_text)
            preApproval = false // visible
            val remaining = Utils.getMilliSec(viewModel!!.preApproved.value!!.createdAt)
            if (remaining < 1440) {
                var remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)

                viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                runnable = Runnable {
                    kotlin.run {
                        remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)
                        viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                        handler.postDelayed(runnable ?: Runnable { }, 1000)
                    }
                }
                handler.postDelayed(runnable ?: Runnable { }, 1000)
            } else {
                viewModel!!.preApprovalVisible.set(false)
            }

            preApprovalClick = View.OnClickListener {
                viewModel!!.sendMsg("preapproval")
                subscribeForData()
            }
        }
    }

    fun setCancelBooking() {
        with(mBinding) {
            preHeaderText = getString(R.string.booking_confirmed_txt)
            prebuttonText = getString(R.string.cancel_reserve)
            declineVisible = false
            presubText = getString(R.string.host_cancel_text)
            preApproval = false
            presubVisible = false
            viewModel!!.timerValue.set("")
            preApprovalClick = View.OnClickListener {
                try {
                    CancellationActivity.openCancellationActivity(
                        this@HostNewInboxMsgActivity,
                        viewModel!!.preApproved.value!!.reservationID,
                        guestProfileID,
                        "host",
                        1
                    )
                    finish()
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setRequestBook() {
        Timber.d("hello prebutton reset")
        with(mBinding) {
            preHeaderText = getString(R.string.request_book_text, guestName)
            presubText = getString(R.string.pre_approved_sub_text, guestName)
            presubVisible = false
            prebuttonText = getString(R.string.approve)
            preApproval = false
            declineVisible = true
            val remaining = Utils.getMilliSec(viewModel!!.preApproved.value!!.createdAt)
            if (remaining < 1440) {
                var remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)

                viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                runnable = Runnable {
                    kotlin.run {
                        remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)
                        viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                        handler.postDelayed(runnable ?: Runnable { }, 1000)
                    }
                }
                handler.postDelayed(runnable ?: Runnable { }, 1000)
            } else {
                viewModel!!.preApprovalVisible.set(false)
            }

            preApprovalClick = View.OnClickListener {

                it.isClickable = false
                viewModel!!.approveReservation(
                    viewModel!!.preApproved.value!!.id,
                    viewModel!!.preApproved.value!!.content,
                    "approved",
                    viewModel!!.preApproved.value!!.startDate,
                    viewModel!!.preApproved.value!!.endDate,
                    viewModel!!.preApproved.value!!.personCapacity,
                    viewModel!!.preApproved.value!!.visitors?:0,
                    viewModel!!.preApproved.value!!.pets?:0,
                    viewModel!!.preApproved.value!!.infants?:0,
                    viewModel!!.preApproved.value!!.reservationID,
                    "approved",
                    it
                )
                refreshThread()

            }

            declineClick = View.OnClickListener {
                it.isClickable = false
                viewModel!!.approveReservation(
                    viewModel!!.preApproved.value!!.id,
                    viewModel!!.preApproved.value!!.content,
                    "declined",
                    viewModel!!.preApproved.value!!.startDate,
                    viewModel!!.preApproved.value!!.endDate,
                    viewModel!!.preApproved.value!!.personCapacity,
                    viewModel!!.preApproved.value!!.visitors?:0,
                    viewModel!!.preApproved.value!!.pets?:0,
                    viewModel!!.preApproved.value!!.infants?:0,
                    viewModel!!.preApproved.value!!.reservationID,
                    "declined",
                    it
                )
                refreshThread()
            }
        }
    }

    fun setRequestSent() {
        handler.removeCallbacks(runnable ?: Runnable { })
        with(mBinding) {
            preHeaderText = getString(R.string.request_approved)
            preApproval = true
            presubText = ""
            viewModel!!.timerValue.set(getString(R.string.approval_text))
        }
    }
    fun approvedVisibility(){
        handler.removeCallbacks(runnable ?: Runnable { })
        with(mBinding) {
            viewModel?.preApprovalVisible?.set(false)
            preApproval = false
            declineVisible = false

        }
    }

    fun declinedBooking() {
        handler.removeCallbacks(runnable ?: Runnable { })
        with(mBinding) {
            preHeaderText = getString(R.string.declined_booking)
            preApproval = true
            declineVisible = false
            viewModel!!.timerValue.set(getString(R.string.declined_content))
        }
    }

    fun completedReserv() {
        handler.removeCallbacks(runnable ?: Runnable { })
        with(mBinding) {
            preHeaderText = getString(R.string.reserv_compelted)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.completed_content))
        }
    }

    fun bookingCancelled() {
        handler.removeCallbacks(runnable ?: Runnable { })
        with(mBinding) {
            preHeaderText = getString(R.string.booking_request_cancelled)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.cancelled_content, guestName))
        }
    }

    override fun onRetry() {
        try {
            if (viewModel.isRetry.get() == 1) {
                if (!viewModel!!.msg.get().equals("")) {
                    viewModel.sendMsg()
                }
            } else if (viewModel.isRetry.get() == 2) {
                pageNumber = 1
                compositeDisposable.remove(disposable)
                subscribeForData()
                Timber.d("subscribe3")
                paginationAdapter?.removeItems()
            } else if (viewModel.isRetry.get() == 3) {
                CancellationActivity.openCancellationActivity(
                    this,
                    viewModel.preApproved.value!!.reservationID,
                    guestProfileID,
                    "guest",
                    1
                )
                finish()
            } else if (viewModel.isRetry.get() == 4) {
                viewModel.approveReservation(
                    viewModel.preApproved.value!!.id,
                    viewModel.preApproved.value!!.content,
                    "approved",
                    viewModel.preApproved.value!!.startDate,
                    viewModel.preApproved.value!!.endDate,
                    viewModel.preApproved.value!!.personCapacity,
                    viewModel.preApproved.value!!.visitors?:0,
                    viewModel.preApproved.value!!.pets?:0,
                    viewModel.preApproved.value!!.infants?:0,
                    viewModel.preApproved.value!!.reservationID,
                    "approved"
                )
            } else if (viewModel.isRetry.get() == 5) {
                viewModel.approveReservation(
                    viewModel.preApproved.value!!.id,
                    viewModel.preApproved.value!!.content,
                    "declined",
                    viewModel.preApproved.value!!.startDate,
                    viewModel.preApproved.value!!.endDate,
                    viewModel.preApproved.value!!.personCapacity,
                    viewModel.preApproved.value!!.visitors?:0,
                    viewModel.preApproved.value!!.pets?:0,
                    viewModel.preApproved.value!!.infants?:0,
                    viewModel.preApproved.value!!.reservationID,
                    "declined"
                )
            } else if (viewModel.isRetry.get() == 6) {
                viewModel.sendMsg("preapproval")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        compositeDisposable.clear()
        handler.removeCallbacks(runnable ?: Runnable { })
        super.onDestroy()
    }

    override fun moveToBackScreen() {
        onRetry()
    }

    override fun openBillingActivity() {

    }

    override fun openListingDetails() {

    }

    override fun hideTopView(msg: String) {
        showToast(msg)

//     This code is for update the approved or declined status to the recyclerView pagination adapter
        pageNumber = 1
        compositeDisposable.remove(disposable)
        subscribeForData()
        Timber.d("subscribe2")
        paginationAdapter?.removeItems()


        handler.removeCallbacks(runnable ?: Runnable { })
        if (msg.equals(getString(R.string.reservation_approved))) {
            setCancelBooking()
        } else {
            declinedBooking()
        }
    }

    override fun addMessage(text: SendMessageMutation.Results) {
        if (!text.content.isNullOrEmpty())
            paginationAdapter?.addItem(
                GetThreadsQuery.ThreadItem(
                    text.id,
                    viewModel.inboxInitData.value!!.threadId,
                    text.reservationId,
                    text.content,
                    text.sentBy,
                    text.type,
                    text.startDate,
                    text.endDate,
                    text.createdAt,
                    text.personCapacity,
                    0,
                    0,
                    0
                )
            )


        if (text.type.equals("preApproved")) {
            setRequestSent()
        }

        paginationAdapter!!.notifyDataSetChanged()
        mBinding.rvInboxDetails.layoutManager?.scrollToPosition(0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (from != null && from.equals("fcm")) {
            val intent = Intent(this, HostHomeActivity::class.java)
            intent.putExtra("from", "fcm")
            startActivity(intent)
            finish()
        }
    }

}
