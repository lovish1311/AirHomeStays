package com.airhomestays.app.ui.listing.amenities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentAmenitiesBottomBindingImpl
import com.airhomestays.app.ui.base.BaseBottomSheet
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import javax.inject.Inject


class AmenitiesBottomFragment : BaseBottomSheet<FragmentAmenitiesBottomBindingImpl, ListingDetailsViewModel>() {

        @Inject
        lateinit var mViewModelFactory: ViewModelProvider.Factory
        override val bindingVariable: Int
            get() = BR.viewModel
        override val layoutId: Int
            get() = R.layout.fragment_amenities_bottom
        override val viewModel: ListingDetailsViewModel
            get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
        lateinit var mBinding: FragmentAmenitiesBottomBindingImpl
        private var type: String? = null
        val list = ArrayList<String>()
        private var convertedType: String = ""

        companion object {
            private const val LISTTYPE = "param1"
            @JvmStatic
            fun newInstance(type: String) =
                AmenitiesBottomFragment().apply {
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
        }

        private fun subscribeToLiveData() {
            viewModel.listingDetails.observe(viewLifecycleOwner, Observer { it?.let { details ->
                when (type) {
                    getString(R.string.amenities) -> {
                        convertedType = getString(R.string.amenities)
                        generateUserAmenitiesList(details)
                    }
                    "User Space" -> {
                        convertedType = getString(R.string.user_space)
                        generateUserSpaceList(details)
                    }
                    "User Safety" -> {
                        convertedType = getString(R.string.user_safety)
                        generateUserSafetyList(details)
                    }
                    "House rules" -> {
                        convertedType = getString(R.string.house_rules)
                        generateHouseRulesList(details)
                    }
                }
                initEpoxy(list)
            } })
        }

        private fun generateUserAmenitiesList (details: ViewListingDetailsQuery.Results) {
            details.userAmenities?.forEach { item ->
                item.let { amenity ->
                    if (amenity?.itemName.isNullOrEmpty().not()) {
                        list.add(amenity?.itemName!!)
                    }
                }
            }
        }

        private fun generateUserSpaceList (details: ViewListingDetailsQuery.Results) {
            details.userSpaces?.forEach { item ->
                item.let { amenity ->
                    if (amenity?.itemName.isNullOrEmpty().not()) {
                        list.add(amenity?.itemName!!)
                    }
                }
            }
        }

        private fun generateUserSafetyList (details: ViewListingDetailsQuery.Results) {
            details.userSafetyAmenities?.forEach { item ->
                item.let { amenity ->
                    if (amenity?.itemName.isNullOrEmpty().not()) {
                        list.add(amenity?.itemName!!)
                    }
                }
            }
        }

        private fun generateHouseRulesList (details: ViewListingDetailsQuery.Results) {
            details.houseRules?.forEach { item ->
                item.let { amenity ->
                    if (amenity?.itemName.isNullOrEmpty().not()) {
                        list.add(amenity?.itemName!!)
                    }
                }
            }
        }

        private fun initEpoxy(it: List<String>) {
            mBinding.rlListingAmenities.withModels {
                viewholderHeaderSmall {
                    id("cancellation")
                    header(convertedType)
                }
                for (i in 0 until it.size) {
                    viewholderListingDetailsSublist {
                        id("list")
                        list(it[i])
                        paddingTop(true)
                        paddingBottom(true)
                    }
                }
            }
        }

        override fun onRetry() {

        }
    }