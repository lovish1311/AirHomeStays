package com.airhomestays.app.ui.host.hostListing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.databinding.HostListingListFragmentBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.HostFinalActivity
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderManageList
import com.airhomestays.app.vo.ListingInitData
import java.util.Locale
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"

class HostListingListFragment :
    BaseFragment<HostListingListFragmentBinding, HostListingViewModel>(), HostListingNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostListingListFragmentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_listing_list_fragment
    override val viewModel: HostListingViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(HostListingViewModel::class.java)

    private var param1: String? = null
    var completedPercent: Int = 0
    var progressLoaded: Boolean = true
    var completedLoaded: Boolean = true


    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            HostListingListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        CustomSpringAnimation.spring(mBinding.rvManageList)
        subscribeToLiveData()

        mBinding.postList.onClick {
            val intent = Intent(activity, StepOneActivity::class.java)
            startActivity(intent)

        }

    }

    fun subscribeToLiveData() {
        mBinding.rvManageList.gone()
        mBinding.ltLoading.visible()
        mBinding.shimmer.visible()
        mBinding.rlNoListingMsg.gone()
        mBinding.srlManageList.setOnRefreshListener {
            completedLoaded = true
            progressLoaded = true
            mBinding.rvManageList.gone()
            mBinding.shimmer.visible()
            mBinding.rlNoListingMsg.gone()
            viewModel.listRefresh()
        }
        viewModel.emptyList.observe(viewLifecycleOwner) {
            if (it == 0)
                showLoading()
            else if (it == 2) {
                mBinding.rvManageList.visible()
                mBinding.ltLoading.gone()
                mBinding.shimmer.gone()
                mBinding.ll404Page.gone()
                mBinding.rlNoListingMsg.gone()
            } else
                showNoListMessage()
        }
        viewModel.loadListing().observe(viewLifecycleOwner, Observer {
            it.let {
                if (mBinding.rvManageList.adapter != null) {
                    if (viewModel.allList.value!!.size > 0) {
                        mBinding.rvManageList.visible()
                        mBinding.ltLoading.gone()
                        mBinding.shimmer.gone()
                        mBinding.ll404Page.gone()
                        mBinding.rlNoListingMsg.gone()
                        mBinding.rvManageList.requestModelBuild()
                    } else {
                        mBinding.ltLoading.gone()
                        mBinding.shimmer.gone()
                        showNoListMessage()
                    }

                } else {
                    mBinding.rvManageList.visible()
                    mBinding.ltLoading.gone()
                    mBinding.shimmer.gone()
                    mBinding.ll404Page.gone()
                    mBinding.rlNoListingMsg.gone()
                    setUp()
                }
            }
        })
    }

    fun setUp() {
        mBinding.postList.onClick {
            val intent = Intent(activity, StepOneActivity::class.java)
            startActivity(intent)

        }
        try {
            mBinding.rvManageList.withModels {

                mBinding.rlNoListingMsg.visible()
                mBinding.postList.onClick {

                    val intent = Intent(activity, StepOneActivity::class.java)
                    startActivity(intent)

                }
                if (viewModel.allList.value!!.size <= 0) {
                    mBinding.ltLoading.gone()
                    mBinding.shimmer.gone()
                    showNoListMessage()
                }


                if (viewModel.allList.value != null) {

                    if (param1 == "inProgress") {

                        viewModel.allList.value!!.forEachIndexed { index, result ->
                            if (result.isReady!!.not()) {
                                if (progressLoaded) {
                                    progressLoaded = false
                                }
                                if (result.isReady!!) {
                                    completedPercent = 100
                                } else if (result.step1Status.equals("completed") && result.step2Status.equals(
                                        "completed"
                                    ) &&
                                    result.step3Status.equals("completed") && !result.isReady!!
                                ) {
                                    if (result.imageName.equals("")) {
                                        completedPercent = 90
                                    } else {
                                        completedPercent = 100
                                    }
                                } else if (result.step1Status.equals("active")) {
                                    completedPercent = 20
                                } else if (result.step1Status.equals("completed")) {
                                    if (result.step2Status.equals("completed")) {
                                        if (result.imageName.equals("")) {
                                            if (result.step3Status.equals("completed")) {
                                                completedPercent = 60
                                            } else {
                                                completedPercent = 50
                                            }
                                        } else {
                                            completedPercent = 60
                                        }
                                    } else if (!result.imageName.equals("")) {
                                        completedPercent = 40
                                    } else {
                                        completedPercent = 30
                                    }
                                }
                                val date = Utils.listingEpochToDate(
                                    result.created!!.toLong(),
                                    Utils.getCurrentLocale(requireContext())
                                        ?: Locale.US
                                )
                                viewholderManageList {
                                    id("list$index")
                                    mBinding.rlNoListingMsg.gone()
                                    if (result.title.equals("")) {
                                        val tit = "${result.roomType} in ${result.location}"
                                        title(tit)
                                    } else {
                                        title(result.title)
                                    }
                                    image(result.imageName)
                                    created(getString(R.string.last_updated_on) + " $date")
                                    percent(
                                        getString(R.string.You_re) + " $completedPercent% " + getString(
                                            R.string.done_with_your_listing
                                        )
                                    )
                                    listPercent(completedPercent)
                                    publishVisible(true)
                                    preview(result.isReady!!.not()) //result.isReady!!.not()
                                    previewClick(View.OnClickListener {
                                        Utils.clickWithDebounce(it) {
                                            val id = viewModel.allList.value!![index].id
                                            viewModel.retryCalled = "view-$id"
                                            viewModel.getListingDetails(id)
                                        }
                                    })
                                    onclick(View.OnClickListener {
                                        val intent = Intent(context, HostFinalActivity::class.java)
                                        intent.putExtra(
                                            "listId",
                                            viewModel.allList.value!![index].id.toString()
                                        )
                                        intent.putExtra("yesNoString", "Yes")
                                        intent.putExtra("bathroomCapacity", "0")
                                        intent.putExtra("country", "")
                                        intent.putExtra("countryCode", "")
                                        intent.putExtra("street", "")
                                        intent.putExtra("buildingName", "")
                                        intent.putExtra("city", "")
                                        intent.putExtra("state", "")
                                        intent.putExtra("zipcode", "")
                                        intent.putExtra("lat", "")
                                        intent.putExtra("lng", "")
                                        startActivity(intent)
                                    })
                                    onBind { _, view, _ ->
                                        val relativeLay =
                                            view.dataBinding.root.findViewById<TextView>(R.id.tv_preview_listing1)
                                        relativeLay.setOnClickListener {
                                            showDeleteConfirm(
                                                viewModel.allList.value!![index].id,
                                                index,
                                                "inprogress"
                                            )
                                            true
                                        }

                                    }
                                    onUnbind { _, view ->
                                        val relativeLay =
                                            view.dataBinding.root.findViewById<RelativeLayout>(R.id.deleteClickLay)
                                        relativeLay.setOnLongClickListener(null)
                                        relativeLay.setOnClickListener(null)
                                    }
                                }
                            }
                        }
                    } else if (param1 == "completed") {
                        viewModel.allList.value!!.forEachIndexed { index, result1 ->
                            if (result1.isReady!!) {
                                if (completedLoaded) {
                                    completedLoaded = false
                                }
                                val date = Utils.listingEpochToDate(
                                    result1.created!!.toLong(),
                                    Utils.getCurrentLocale(requireContext()) ?: Locale.US
                                )

                                var submissionStateText =
                                    if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && result1.isPublish == false)
                                        "approved"
                                    else if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && result1.isPublish == true)
                                        "published"
                                    else if (result1.listApprovelStatus == "approved" && result1.isPublish == true)
                                        "published"
                                    else result1.listApprovelStatus

                                viewholderManageList {
                                    id("completedlist" + index)
                                    mBinding.rlNoListingMsg.gone()
                                    title(result1.title)
                                    image(result1.imageName)
                                    submissionStatus(submissionStateText)
                                    percent(getString(R.string.you_re_100_done_with_your_listing))
                                    listPercent(100)
                                    created(getString(R.string.last_updated_on) + " $date")
                                    publishVisible(false)
                                    onPublishClick(View.OnClickListener {
                                        if(viewModel.allList.value?.get(index)?.listApprovelStatus.equals("approved").not()){
                                            if ((viewModel.allList.value?.get(index)?.listApprovelStatus == null || viewModel?.allList?.value?.get(index)?.listApprovelStatus == "declined") && viewModel.dataManager.listingApproval == DataManager.ListingApproval.REQUIRED) {
                                                viewModel.submitForVerification("pending", viewModel?.allList?.value!!.get(index).id)
                                                return@OnClickListener
                                            }
                                        }

                                        if (viewModel.publishBoolean.get()!!) {
                                            viewModel.publishBoolean.set(false)
                                            if (viewModel.allList!!.value!![index]!!.isPublish!!) {
                                                viewModel.retryCalled =
                                                    "update-unPublish-$id-$index"
                                                viewModel.publishListing(
                                                    "unPublish",
                                                    viewModel.allList.value!![index].id,
                                                    index
                                                )
                                            } else {
                                                    viewModel.retryCalled = "update-publish-$id-$index"
                                                    viewModel.publishListing(
                                                        "publish",
                                                        viewModel.allList.value!![index].id,
                                                        index
                                                    )
                                            }
                                        }
                                    })
                                    previewClick(View.OnClickListener {
                                        initiatePopupWindow(it, index, "completed")
                                    })
                                    publishTxt(result1.isPublish!!)
                                    preview(false) //result1.isReady!!.not()
                                    onclick(View.OnClickListener {
                                        val intent = Intent(context, HostFinalActivity::class.java)
                                        intent.putExtra("listId",
                                            viewModel.allList.value!![index].id.toString()
                                        )
                                        intent.putExtra("yesNoString", "Yes")
                                        intent.putExtra("bathroomCapacity", "0")
                                        intent.putExtra("country", "")
                                        intent.putExtra("countryCode", "")
                                        intent.putExtra("street", "")
                                        intent.putExtra("buildingName", "")
                                        intent.putExtra("city", "")
                                        intent.putExtra("state", "")
                                        intent.putExtra("zipcode", "")
                                        intent.putExtra("lat", "")
                                        intent.putExtra("lng", "")
                                        startActivity(intent)
                                    })
                                    onBind { _, view, _ ->
                                        val relativeLay =
                                            view.dataBinding.root.findViewById<TextView>(R.id.tv_preview_listing1)
                                        val more =
                                            view.dataBinding.root.findViewById<ImageView>(R.id.image1)

                                        more.gone()
                                        relativeLay.gone()

                                    }
                                    onUnbind { _, view ->
                                        val relativeLay =
                                            view.dataBinding.root.findViewById<RelativeLayout>(R.id.deleteClickLay)
                                        relativeLay.setOnLongClickListener(null)
                                        relativeLay.setOnClickListener(null)
                                    }
                                }
                            }
                        }
                    }
                }
                completedLoaded = true
                progressLoaded = true
                if (mBinding.srlManageList.isRefreshing) {
                    mBinding.srlManageList.isRefreshing = false
                }
            }
        } catch (e: Exception) {
            showError()
        }
    }

    private fun initiatePopupWindow(anchor: View, index: Int, from: String): PopupWindow? {
        val mInflater = baseActivity
            ?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = mInflater.inflate(R.layout.host_listing_list, null)

        val mDropdown = PopupWindow(
            layout,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        try {
            val preview: TextView = layout.findViewById<View>(R.id.preview) as TextView
            val delete: TextView = layout.findViewById<View>(R.id.delete) as TextView
            preview.text = anchor.resources.getString(R.string.preview)
            delete.text = anchor.resources.getString(R.string.delete)
            preview.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_preview
                ), null, null, null
            )
            delete.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_cancel
                ), null, null, null
            )

            //If you want to add any listeners to your textviews, these are two //textviews.
            preview.setOnClickListener {
                mDropdown.dismiss()
                Utils.clickWithDebounce(it) {
                    val id = viewModel.allList.value!![index].id
                    viewModel.retryCalled = "view-$id"
                    viewModel.getListingDetails(id)
                }
            }
            delete.setOnClickListener {
                mDropdown.dismiss()
                showDeleteConfirm(viewModel.allList.value!![index].id, index, from)
            }
            layout.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            mDropdown.showAsDropDown(anchor, -50, 5)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return mDropdown
    }

    fun showDeleteConfirm(listId: Int, pos: Int, from: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_listing))
            .setMessage(getString(R.string.confrim_delete))
            .setPositiveButton(getString(R.string.DELETE)) { _, _ ->
                viewModel.retryCalled = getString(R.string.delete) + "-$listId-$pos-$from"
                viewModel.removeList(listId, pos, from)
            }
            .setNegativeButton(getString(R.string.CANCEL)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun showListDetails() {
        val item = viewModel.listingDetails
        item?.value?.let {
            val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(
                viewModel.getConvertedRate(
                    it.listingData?.currency!!, it.listingData?.basePrice!!.toDouble()
                )
            )
            val photo = ArrayList<String>()
            photo.add(it.listPhotoName!!)
            val listDetails = ListingInitData(
                title = it.title.toString(),
                id = it.id!!.toInt(),
                photo = photo,
                roomType = it.roomType.toString(),
                ratingStarCount = it.reviewsStarRating,
                reviewCount = it.reviewsCount,
                price = currency,
                guestCount = 0,
                startDate = "0",
                endDate = "0",
                selectedCurrency = viewModel.getUserCurrency(),
                currencyBase = viewModel.getCurrencyBase(),
                currencyRate = viewModel.getCurrencyRates(),
                hostName = it.user!!.profile!!.displayName.toString(),
                bookingType = it.bookingType!!,
                isPreview = true
            )
            ListingDetails.openListDetailsActivity(requireContext(), listDetails)

        }
    }

    override fun show404Screen() {
        mBinding.srlManageList.gone()
        mBinding.rvManageList.gone()
        mBinding.rlNoListingMsg.gone()

        mBinding.ll404Page.visible()
    }

    override fun showNoListMessage() {
        mBinding.rlNoListingMsg.visible()
        mBinding.rvManageList.gone()
        mBinding.ltLoading.gone()
        mBinding.shimmer.gone()
        mBinding.ll404Page.gone()


        if (mBinding.srlManageList.isRefreshing) {
            mBinding.srlManageList.isRefreshing = false
        }

    }

    fun showLoading() {
        mBinding.rvManageList.gone()
        mBinding.ltLoading.visible()
        mBinding.shimmer.visible()
        mBinding.ll404Page.gone()


        if (mBinding.srlManageList.isRefreshing) {
            mBinding.srlManageList.isRefreshing = false
        }

    }

    fun onRefresh() {
        completedLoaded = true
        progressLoaded = true
        mBinding.shimmer.visible()
        if (::mViewModelFactory.isInitialized) {
            if (mBinding.srlManageList.isRefreshing.not()) {
                mBinding.rvManageList.gone()
                mBinding.ltLoading.visible()
                mBinding.shimmer.visible()
                mBinding.rlNoListingMsg.gone()
                viewModel.listRefresh()
            }
        }
    }

    override fun onRetry() {
        if (viewModel.retryCalled.equals("")) {
            mBinding.shimmer.visible()
            mBinding.rlNoListingMsg.gone()
            viewModel.getList()
        } else if (viewModel.retryCalled.contains("delete")) {
            val text = viewModel.retryCalled.split("-")
            viewModel.removeList(text[1].toInt(), text[2].toInt(), text[3])
        } else if (viewModel.retryCalled.contains("update")) {
            val text = viewModel.retryCalled.split("-")
            viewModel.publishListing(text[1], text[2].toInt(), text[3].toInt())
        } else if (viewModel.retryCalled.contains("view")) {
            val text = viewModel.retryCalled.split("-")
            viewModel.getListingDetails(text[1].toInt())
        }
    }

    fun onBackPressed() {
        if (mBinding.ll404Page.visibility == View.VISIBLE) {
            mBinding.srlManageList.visible()
            mBinding.rvManageList.visible()
            mBinding.rlNoListingMsg.gone()
            mBinding.ll404Page.gone()
        } else {
            baseActivity?.finish()
        }
    }


    override fun onDestroyView() {
        mBinding.rvManageList.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        mBinding.rvManageList.adapter = null
        super.onDestroy()
    }

    override fun hideLoading() {
        mBinding.ltLoading.gone()
        mBinding.shimmer.gone()
    }
}