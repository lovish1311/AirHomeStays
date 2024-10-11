package com.airhomestays.app.ui.host.hostListing

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostListingFragmentBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.ListingInitData
import javax.inject.Inject

class HostListingFragment : BaseFragment<HostListingFragmentBinding, HostListingViewModel>(),HostListingNavigator {


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostListingFragmentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_listing_fragment
    override val viewModel: HostListingViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(HostListingViewModel::class.java)

    var completedPercent : Int = 0
    var progressLoaded : Boolean = true
    var completedLoaded : Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this

        val myAdapter = MyAdapter(childFragmentManager)
        with(mBinding.viewPager) {
            adapter = myAdapter
            mBinding.tabs.post { mBinding.tabs.setupWithViewPager(this) }
            addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {
                    timber.log.Timber.tag("tripsPage1").d(p0.toString())
                }
                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    // Timber.tag("tripsPage12").d(p0.toString())
                }
                override fun onPageSelected(p0: Int) {
                    timber.log.Timber.tag("tripsPage123").d(p0.toString())
                }
            })
        }

        mBinding.ivAddList.setOnClickListener {
            val intent = Intent(activity, StepOneActivity::class.java)
            activity?.startActivity(intent)
        }
        mBinding.postList.setOnClickListener {
            val intent = Intent(activity, StepOneActivity::class.java)
            activity?.startActivity(intent)
        }
    }







    override fun showListDetails() {
        val item = viewModel.listingDetails
        item?.value?.let {
            val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(it.listingData?.currency!!, it.listingData?.basePrice!!.toDouble()))
            val photo = ArrayList<String>()
            photo.add(it.listPhotoName!!)
            var publishStatus : Boolean
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
                    isPreview = true)

            ListingDetails.openListDetailsActivity(requireContext(),listDetails)

        }
    }

    override fun show404Screen() {
        mBinding.rlNoListingMsg.gone()

        mBinding.ll404Page.visible()
    }

    override fun showNoListMessage() {
        mBinding.rlNoListingMsg.visible()

        mBinding.ll404Page.gone()

    }

    fun onRefresh() {
        completedLoaded = true
        progressLoaded = true
        if (::mViewModelFactory.isInitialized) {
            viewModel.listRefresh()
        }
    }

    override fun onRetry() {
        if(viewModel.retryCalled.equals("")){
            viewModel.getList()
        }else if(viewModel.retryCalled.contains("delete")){
            val text =viewModel.retryCalled.split("-")
            viewModel.removeList(text[1].toInt(),text[2].toInt(),text[3])
        }else if(viewModel.retryCalled.contains("update")){
            val text =viewModel.retryCalled.split("-")
            viewModel.publishListing(text[1],text[2].toInt(),text[3].toInt())
        }else if(viewModel.retryCalled.contains("view")){
            val text =viewModel.retryCalled.split("-")
            viewModel.getListingDetails(text[1].toInt())
        }
    }

    fun onBackPressed(){
        if(mBinding.ll404Page.visibility == View.VISIBLE){
            mBinding.rlNoListingMsg.gone()
            mBinding.ll404Page.gone()
        }else{
            baseActivity?.finish()
        }
    }


    override fun hideLoading() {
    }


    inner class MyAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> HostListingListFragment.newInstance("inProgress")
                1 -> HostListingListFragment.newInstance("completed")
                else -> Fragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 ->  if(isAdded && activity!=null){ return baseActivity!!.resources.getString(R.string.in_progress) }
                1 ->  if(isAdded && activity!=null){ return baseActivity!!.resources.getString(R.string.completed) }
            }
            return null
        }
    }

}