package com.airhomestays.app.ui.listing.desc

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentListingAmenitiesBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderListingDetailsSectionHeader
import com.airhomestays.app.viewholderListingDetailsSublist
import javax.inject.Inject

class DescriptionFragment: BaseFragment<FragmentListingAmenitiesBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentListingAmenitiesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.rlShowresult.gone()
        mBinding.ivClose.visible()
        mBinding.ivClose.onClick { baseActivity?.onBackPressed() }
        mBinding.rlListingAmenities.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { text -> text?.let { initEpoxy(it.description) } })
    }

    private fun initEpoxy(it: String?) {
        mBinding.rlListingAmenities.withModels {
            viewholderListingDetailsSectionHeader {
                id("header")
                header(resources.getString(R.string.about_list))
            }
            if (it.isNullOrEmpty().not()) viewholderListingDetailsSublist {
                id("desc")
                list(it)
                needImage(false)
                onBind{_,view,_ ->
                    val textView= view.dataBinding.root.findViewById<ImageView>(R.id.image)
                    textView.gone()
                }
                paddingTop(true)
            }
        }
    }

    override fun onRetry() {

    }
    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }
}