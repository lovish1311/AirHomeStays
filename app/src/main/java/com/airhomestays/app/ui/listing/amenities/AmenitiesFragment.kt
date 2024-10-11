package com.airhomestays.app.ui.listing.amenities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.databinding.FragmentListingAmenitiesBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderListingDetailsSectionHeader
import com.airhomestays.app.viewholderListingDetailsSublist
import javax.inject.Inject

class AmenitiesFragment: BaseFragment<FragmentListingAmenitiesBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: FragmentListingAmenitiesBinding
    private var type: String? = null
    val list = ArrayList<String>()
    val imageList =ArrayList<String>()
    private var convertedType: String = ""

    companion object {
        private const val LISTTYPE = "param1"
        @JvmStatic
        fun newInstance(type: String) =
                AmenitiesFragment().apply {
                    arguments = Bundle().apply {
                        putString(LISTTYPE, type)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        arguments?.let {
            type = it.getString(LISTTYPE)
        }
        mBinding.rlShowresult.gone()
        mBinding.ivClose.visible()
        mBinding.ivClose.onClick { baseActivity?.onBackPressed() }
        mBinding.rlListingAmenities.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { it?.let { details ->
            when (type) {
                getString(R.string.amenities) -> {
                    convertedType = getString(R.string.amenities)
                    generateUserAmenitiesList(details)
                }
                getString(R.string.user_space) -> {
                    convertedType = getString(R.string.user_space)
                    generateUserSpaceList(details)
                }
                getString(R.string.safety_amenities) -> {
                    convertedType = getString(R.string.safety_amenities)
                    generateUserSafetyList(details)
                }
                getString(R.string.house_rules) -> {
                    convertedType = getString(R.string.house_rules)
                    generateHouseRulesList(details)
                }
                getString(R.string.bed_types) -> {
                    convertedType = getString(R.string.bed_types)
                    generateUserBedsList(details)
                }
            }
            initEpoxy(list,imageList)
        } })
    }

    private fun generateUserBedsList (details: ViewListingDetailsQuery.Results) {
        details.userBedsTypes?.forEach { item ->
            item.let { amenity ->
                if (amenity!=null) {
                    list.add(amenity.bedName+": "+amenity.bedCount)
                }
            }
        }
    }

    private fun generateUserAmenitiesList (details: ViewListingDetailsQuery.Results) {
        details.userAmenities?.forEach { item ->
            item.let { amenity ->
                if (amenity?.itemName.isNullOrEmpty().not()&&amenity?.image.isNullOrEmpty().not()) {
                    list.add(amenity?.itemName!!)
                    imageList.add(amenity.image!!)
                }else if (amenity?.itemName.isNullOrEmpty().not()){
                    list.add(amenity?.itemName!!)
                    imageList.add("")
                }
            }
        }
    }

    private fun generateUserSpaceList (details: ViewListingDetailsQuery.Results) {
        details.userSpaces?.forEach { item ->
            item?.let { amenity ->
                if (amenity.itemName.isNullOrEmpty().not()&&amenity.image.isNullOrEmpty().not()) {
                    list.add(amenity.itemName!!)
                    imageList.add(amenity.image!!)
                }else if (amenity.itemName.isNullOrEmpty().not()){
                    list.add(amenity.itemName!!)
                    imageList.add("")
                }
            }
        }
    }

    private fun generateUserSafetyList (details: ViewListingDetailsQuery.Results) {
        details.userSafetyAmenities?.forEach { item ->
            item?.let { amenity ->
                if (amenity.itemName.isNullOrEmpty().not()&&amenity.image.isNullOrEmpty().not()) {
                    list.add(amenity.itemName!!)
                    imageList.add(amenity.image!!)
                }else if (amenity.itemName.isNullOrEmpty().not()){
                    list.add(amenity.itemName!!)
                    imageList.add("")
                }
            }
        }
    }

    private fun generateHouseRulesList (details: ViewListingDetailsQuery.Results) {
        details.houseRules?.forEach { item ->
            item?.let { amenity ->
                if (amenity.itemName.isNullOrEmpty().not()) {
                    list.add(amenity.itemName!!)
                }
            }
        }
    }

    private fun initEpoxy(it: List<String>, imageList: ArrayList<String>) {
        mBinding.rlListingAmenities.withModels {
            viewholderListingDetailsSectionHeader {
                id("header")
                header(convertedType)
            }
            for (i in 0 until it.size) {
                if (convertedType==getString(R.string.amenities)||convertedType==getString(R.string.safety_amenities)||convertedType==getString(R.string.user_space)){
                    viewholderListingDetailsSublist {
                        id("list")
                        list(it[i])
                        if (imageList[i].isNotEmpty()){
                            amenitiesImage(Constants.amenities+imageList[i])
                            needImage(true)
                        }else{
                            amenitiesImage("")
                            needImage(true)
                        }
                        paddingTop(true)
                    }
                }else {
                    viewholderListingDetailsSublist {
                        id("list")
                        list(it[i])
                        paddingTop(true)
                        paddingBottom(true)
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }
    override fun onRetry() {

    }
}