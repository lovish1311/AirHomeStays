package com.airhomestays.app.ui.host.step_one

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.GetCountrycodeQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentAddressBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderHostAddressEt
import com.airhomestays.app.viewholderHostSelectCountry
import com.airhomestays.app.viewholderUserName2
import com.airhomestays.app.viewholderUserNormalText
import timber.log.Timber
import javax.inject.Inject


class AddressFragment : BaseFragment<HostFragmentAddressBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_address
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentAddressBinding
    private var list = ArrayList<GetCountrycodeQuery.Result?>()
    var strUser: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.actionBar.tvRightside.gone()
        mBinding.pgBar.progress = 30
        if (baseActivity!!.getIntent().hasExtra("from")) {
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
            if (strUser.isNotEmpty() && strUser.equals("steps"))
                viewModel.isEdit = true
            else
                viewModel.isEdit = false
        }

        subscribeToLiveData()
        setUp()

        if (Geocoder.isPresent()) {
            Timber.d("geocoder is present")
        } else {
            Timber.d("geocoder is not present")
        }

        val test = requireFragmentManager().findFragmentByTag("ADDRESS")


        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.status_bar_color
                )
            )
            mBinding.tvRightsideText.setOnClickListener {
                Utils.clickWithDebounce(it) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.address.set("")
                    viewModel.location.set("")
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                    if (viewModel.country.get()!!.trim().isNullOrEmpty()) {
                        baseActivity!!.showSnackbar(
                            getString(R.string.error_1),
                            getString(R.string.please_enter_country)
                        )
                        it.enable()
                    } else if (viewModel.street.get()!!.trim().isEmpty()) {
                        baseActivity!!.showSnackbar(
                            getString(R.string.error_1),
                            getString(R.string.please_enter_street)
                        )
                        it.enable()
                    } else if (viewModel.city.get()!!.trim().isEmpty()) {
                        baseActivity!!.showSnackbar(
                            getString(R.string.error_1),
                            getString(R.string.please_enter_city)
                        )
                        it.enable()
                    } else if (viewModel.state.get()!!.trim().isEmpty()) {
                        baseActivity!!.showSnackbar(
                            getString(R.string.error_1),
                            getString(R.string.please_enter_state)
                        )
                        it.enable()
                    } else if (viewModel.zipcode.get()!!.trim().isEmpty()) {
                        baseActivity!!.showSnackbar(
                            getString(R.string.error_1),
                            getString(R.string.please_enter_zip_code)
                        )
                        it.enable()
                    } else {
                        viewModel.updateHostStepOne()
                    }

                }
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.gone()
        }
        mBinding.actionBar.ivNavigateup.onClick {
            viewModel.address.set("")
            viewModel.location.set("")
            viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
            viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
        }


    }

    private fun subscribeToLiveData() {
        viewModel.list.observe(viewLifecycleOwner, Observer {
            it?.let { result ->
                list = ArrayList(result)
                mBinding.rvStepOne.requestModelBuild()
            }
        })
    }

    private fun setUp() {
        setChips()
        mBinding.rvStepOne.withModels {
            viewholderUserName2 {
                id("your place")
                name(getString(R.string.where_your_place_located))
                isBgNeeded(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("street")
                text(getString(R.string.street))
                large(false)
                isBgNeeded(true)
                paddingBottom(true)
            }
            viewholderHostAddressEt {
                id("street et")
                msg(getString(R.string.main_st)).toString()
                observableText(viewModel.street)
            }
            viewholderUserNormalText {
                id("apt")
                text(getString(R.string.apt_suite_etc))
                large(false)
                paddingBottom(true)
            }
            viewholderHostAddressEt {
                id("apt et")
                msg(getString(R.string.apt_suite_etc))
                observableText(viewModel.buildingName)
            }
            viewholderUserNormalText {
                id("city")
                text(getString(R.string.city))
                large(false)
                paddingBottom(true)
            }
            viewholderHostAddressEt {
                id("city et")
                msg(getString(R.string.san_francisco))
                observableText(viewModel.city)
            }
            viewholderUserNormalText {
                id("state")
                text(getString(R.string.state))
                large(false)
                paddingBottom(true)
            }
            viewholderHostAddressEt {
                id("state et")
                msg(getString(R.string.ca))
                observableText(viewModel.state)
            }
            viewholderUserNormalText {
                id("zip")
                text(getString(R.string.zip_postal_code))
                large(false)
                paddingBottom(true)
            }
            viewholderHostAddressEt {
                id("code et")
                msg("94101")
                observableText(viewModel.zipcode)
            }
            viewholderUserNormalText {
                id("country")
                text(getString(R.string.country_region))
                large(false)
                isBgNeeded(false)
                paddingBottom(true)
            }
            viewholderHostSelectCountry {
                id("country select")
                msg(getString(R.string.afghanistan))
                if (list.isNotEmpty()) {
                    for (index in 0 until list.size) {
                        if (viewModel.country.get().toString().isNotEmpty()) {
                            if (viewModel.country.get().toString()
                                    .equals(list[index]?.countryCode)
                            ) {
                                viewModel.country.set(list[index]?.countryName)
                            }
                        }
                    }
                }
                observableText(viewModel.country)
                onBind { _, view, _ ->
                    val textView = view.dataBinding.root.findViewById<TextView>(R.id.et_title)
                    textView.setOnClickListener {
                        Utils.clickWithDebounce(textView) {
                            hideSnackbar()
                            hideKeyboard()
                            viewModel.onContinueClick(StepOneViewModel.NextScreen.SELECT_COUNTRY)
                            textView.isEnabled = false
                        }
                    }
                    textView.isEnabled = true
                }
            }


        }
    }

    private fun setChips() {
        mBinding.apply {
            placeType = false
            noOfGuests = false
            bedrooms = false
            baths = false
            address = true
            location = false
            amenities = false
            safety = false
            space = false
            placeTypeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            })
            noOfGuestsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
            })
            addressClick = (View.OnClickListener {

            })
            locationClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.MAP_LOCATION)
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


    override fun onRetry() {

    }

}