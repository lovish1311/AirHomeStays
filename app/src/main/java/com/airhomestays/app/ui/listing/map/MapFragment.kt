package com.airhomestays.app.ui.listing.map

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ListingListingMapBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class MapFragment: BaseFragment<ListingListingMapBinding, ListingDetailsViewModel>(), OnMapReadyCallback {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.listing_listing_map
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: ListingListingMapBinding
    private lateinit var location: LatLng

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        mBinding.mapView?.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)

    }

    private fun initView() {
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { list -> list?.let {
            try {
                if (it.lat!!.isNaN().not() && it.lng!!.isNaN().not()) {
                    location = LatLng(it.lat!!, it.lng!!)
                }
                mBinding.title = it.title + " "+resources.getString(R.string.inn)+ " " + it.city + ", " + it.state + ", " + it.country
            } catch (e: Exception) {
                showError()
            }
        } })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMaxZoomPreference(18f)
        googleMap.setMinZoomPreference(14f)
        val mapStyleOptions =
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
        googleMap.setMapStyle(mapStyleOptions)
        if(::location.isInitialized) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            googleMap.addCircle(CircleOptions()
                    .center(location)
                    .strokeColor(Utils.getColor(requireContext(), R.color.map_fill))
                    .strokeWidth(3f)
                    .fillColor(Utils.getColor(requireContext(), R.color.map_fill))
                    .radius(500.0))
        }
    }

    override fun onResume() {
        viewModel.clearStatusBar(requireActivity())
        mBinding.mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        mBinding.mapView?.onPause()
        super.onPause()
    }

    override fun onStart() {
        mBinding.mapView?.onStart()
        super.onStart()
    }

    override fun onStop() {
        mBinding.mapView?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mBinding.mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mBinding.mapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mBinding.mapView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRetry() {

    }

}