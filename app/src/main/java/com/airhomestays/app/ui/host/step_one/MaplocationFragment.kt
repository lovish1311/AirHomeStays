package com.airhomestays.app.ui.host.step_one

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentMapLocationBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.Utils.Companion.clickWithDebounce
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class MaplocationFragment : BaseFragment<HostFragmentMapLocationBinding, StepOneViewModel>(),
    OnMapReadyCallback {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_map_location
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentMapLocationBinding
    private lateinit var mMap: GoogleMap
    var strUser: String = ""
    lateinit var p1: LatLng
    lateinit var drgCircle: Circle
    lateinit var drgMarkers: Marker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.actionBar.tvRightside.gone()
        mBinding.pgBar.progress = 40
        setChips()
        viewModel.isMapMoved.observe(viewLifecycleOwner, Observer { map ->
            mBinding.map = map
            mBinding.rlRootLayout.invalidate()
        })
        viewModel.p1.observe(viewLifecycleOwner, Observer {
            p1 = it
        })
        if (baseActivity!!.getIntent().hasExtra("from")) {
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
            viewModel.isEdit = strUser.isNotEmpty() && strUser.equals("steps")
        }
        if (viewModel.isEdit) {
            p1 = LatLng(viewModel.lat.get()!!.toDouble(), viewModel.lng.get()!!.toDouble())
            viewModel.latLng.set(p1.toString())
            viewModel.address.set("")
            viewModel.location.set("")
            viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
        }

        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.status_bar_color
                )
            )
            mBinding.tvRightsideText.setOnClickListener {
                it.disable()
                viewModel.retryCalled = "update"
                if (viewModel.street.get()!!.trim().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(
                        getString(R.string.error_1),
                        getString(R.string.please_enter_street)
                    )
                    it.enable()
                } else if (viewModel.city.get()!!.trim().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(
                        getString(R.string.error_1),
                        getString(R.string.please_enter_city)
                    )
                    it.enable()
                } else if (viewModel.state.get()!!.trim().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(
                        getString(R.string.error_1),
                        getString(R.string.please_enter_state)
                    )
                    it.enable()
                } else if (viewModel.country.get()!!.trim().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(
                        getString(R.string.error_1),
                        getString(R.string.please_enter_country)
                    )
                    it.enable()
                } else if (mBinding.mapToaster.text != getString(R.string.map_toaster_change) ) {
                    it.enable()
                    if (viewModel.isEdit){
                        viewModel.updateHostStepOne()
                    } else {
                        showSnackbar("", getString(R.string.drag_the_pin_to_set_your_location))
                    }
                } else {
                    viewModel.updateHostStepOne()
                }
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.gone()

        }
        mBinding.actionBar.rlToolbarNavigateup.onClick {
            if (viewModel.isEdit) {
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.ADDRESS)
            } else {
                baseActivity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
        mBinding.text = getString(R.string.is_the_pin_in_the_right_place)
        mBinding.title =
            getString(R.string.entire_place) + getString(R.string.inn) + getString(R.string.madurai) + ", " + getString(
                R.string.tn
            ) + ", " + getString(R.string.entire_place)

        mBinding.mapView?.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)

        mBinding.tvNext.onClick {
            if (viewModel.street.get()!!.trim().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(
                    getString(R.string.error_1),
                    getString(R.string.please_enter_street)
                )
            } else if (viewModel.city.get()!!.trim().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(
                    getString(R.string.error_1),
                    getString(R.string.please_enter_city)
                )
            } else if (viewModel.state.get()!!.trim().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(
                    getString(R.string.error_1),
                    getString(R.string.please_enter_state)
                )
            } else if (viewModel.country.get()!!.trim().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(
                    getString(R.string.error_1),
                    getString(R.string.please_enter_country)
                )
            } else if (viewModel.listId.get()!!.isEmpty() || viewModel.listId.get()!!.isBlank()) {
                if (mBinding.mapToaster.text == getString(R.string.map_toaster_change) || viewModel.isEdit) {
                    mBinding.tvNext.setOnClickListener {
                        clickWithDebounce(it) {
                            viewModel.createHostStepOne()
                        }
                    }
                } else {
                    showSnackbar("", getString(R.string.drag_the_pin_to_set_your_location))
                }
            } else if (mBinding.mapToaster.text == getString(R.string.map_toaster_change) || viewModel.isEdit) {
                viewModel.onContinueClick(StepOneViewModel.NextScreen.AMENITIES)
            } else {
                showSnackbar("", getString(R.string.drag_the_pin_to_set_your_location))
            }
        }

    }

    private fun setChips() {
        mBinding.apply {
            placeType = false
            noOfGuests = false
            bedrooms = false
            baths = false
            address = false
            location = true
            amenities = false
            safety = false
            space = false
            placeTypeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            })
            noOfGuestsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
            })
            bedroomsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_BEDS)
            })
            bathsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.NO_OF_BATHROOM)
            })
            addressClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.ADDRESS)
            })
            locationClick = (View.OnClickListener {

            })
            amenitiesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.AMENITIES)
            })
            safetyClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.SAFETY_PRIVACY)
            })
            spaceClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.GUEST_SPACE)
            })
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mapStyleOptions =
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
        mMap.setMapStyle(mapStyleOptions)
        googleMap.setMaxZoomPreference(18f)
        googleMap.setMinZoomPreference(5f)
        googleMap.isMyLocationEnabled


        Handler(Looper.getMainLooper()).postDelayed(Runnable {

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p1, 14.0f))
            val bitmap =
                requireActivity().getBitmapFromVectorDrawable(R.drawable.ic_map_red)
            val a =
                MarkerOptions().position(p1).icon(BitmapDescriptorFactory.fromBitmap(bitmap!!))
            drgMarkers = mMap.addMarker(a)!!

            drgCircle = mMap.addCircle(
                CircleOptions()
                    .center(p1)
                    .zIndex(500f)
                    .strokeColor(Utils.getColor(requireContext(), R.color.colorPrimary))
                    .strokeWidth(8f)
                    .fillColor(Utils.getColor(requireContext(), R.color.map_fill))
                    .radius(700.0)
            )
            mMap.setOnCameraIdleListener {
                drgMarkers?.position = mMap.cameraPosition.target
                drgCircle.isVisible = true
                drgCircle.center = googleMap.cameraPosition.target
                viewModel.p1.value = drgMarkers?.position ?: LatLng(0.0, 0.0)
                viewModel.address.set(p1.toString())
                viewModel.lat.set(p1.latitude.toString())
                viewModel.lng.set(p1.longitude.toString())
            }


        }, 400)

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            mMap.setOnCameraMoveListener {
                drgMarkers?.position = mMap.cameraPosition.target
                drgCircle.isVisible = false
                viewModel.isMapMoved.value = true
                mBinding.map = viewModel.isMapMoved.value
                viewModel.p1.value = drgMarkers.position ?: LatLng(0.0, 0.0)
                viewModel.address.set(p1.toString())
                viewModel.lat.set(p1.latitude.toString())
                viewModel.lng.set(p1.longitude.toString())
                viewModel.becomeHostStep1.value!!.lng = viewModel.lng.get()!!.toDouble()
                viewModel.becomeHostStep1.value!!.lat = viewModel.lat.get()!!.toDouble()
            }
        }, 500)


    }


    fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(this, drawableId) ?: return null

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) ?: return null
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


    override fun onResume() {
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