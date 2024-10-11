package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentTypeOfBedsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderDividerListTv
import com.airhomestays.app.viewholderHostPlusMinus
import com.airhomestays.app.viewholderListTv
import javax.inject.Inject

class NoOfGuestFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_beds
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfBedsBinding
    var strUser = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.actionBar.tvRightside.visibility = View.GONE
        mBinding.pgBar.progress = 20
        mBinding.viewNeeded = true
        mBinding.text = baseActivity!!.getString(R.string.how_many_guests_can_stay)
        if (baseActivity!!.getIntent().hasExtra("from")) {
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
            if (strUser.isNotEmpty() && strUser.equals("steps"))
                viewModel.isEdit = true
            else
                viewModel.isEdit = false
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
                Utils.clickWithDebounce(it) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.address.set("")
                    viewModel.location.set("")
                    if (viewModel.isEdit) {
                        viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                    } else {
                        viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                    }
                    viewModel.getLocationFromGoogle(
                        viewModel.address.get().toString()
                    )

                    if (viewModel.country.get()!!.trim().isNullOrEmpty() || viewModel.street.get()!!
                            .trim().isNullOrEmpty() || viewModel.city.get()!!.trim()
                            .isNullOrEmpty() || viewModel.state.get()!!.trim()
                            .isNullOrEmpty() || viewModel.zipcode.get()!!.trim().isNullOrEmpty()
                    ) {
                        baseActivity!!.showSnackbar(
                            resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page),
                            resources.getString(R.string.please_fill_them)
                        )
                    } else {
                        if (!isNetworkConnected) {
                            baseActivity!!.showSnackbar(
                                resources.getString(R.string.error),
                                resources.getString(R.string.currently_offline)
                            )
                        } else {
                            viewModel.updateHostStepOne()
                        }
                    }
                }
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.gone()

        }

        mBinding.actionBar.ivNavigateup.onClick {
            if (viewModel.isEdit) {
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            } else {
                baseActivity?.onBackPressed()
            }
        }
        mBinding.tvNext.onClick {
            viewModel.onContinueClick(StepOneViewModel.NextScreen.ADDRESS)
        }
        viewModel.personCapacity1.set("1")
        viewModel.roomCapacity.set("1")
        viewModel.bedCapacity.set("1")
        viewModel.bathroomCapacity.set("1")
        viewModel.editBedCount = 0
        viewModel.updateCount.value!!.forEachIndexed { index, s ->
            viewModel.editBedCount = viewModel.editBedCount + s.toInt()
        }
        subscribeToLiveData()
        setUp()
    }

    private fun subscribeToLiveData() {
        viewModel.noOfBathroom.observe(viewLifecycleOwner, Observer {
            it.let { bath ->
                viewModel.bathroomCapacity.set(bath?.listSettings?.get(0)?.startValue.toString())
            }
            requestModelBuildIt()
        })
        viewModel.bathroomCount.observe(viewLifecycleOwner, Observer {
            viewModel.bathroomCapacity.set(viewModel.bathroomCount.value)
            requestModelBuildIt()
        })
        viewModel.bathroomType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.personCapacity.observe(viewLifecycleOwner, Observer {
            it.let {
                viewModel.personCapacity1.set(
                    it?.listSettings!!.get(0)?.startValue!!.toInt().toString()
                )
            }
            requestModelBuildIt()
        })
        viewModel.guestCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.personCapacity1.set(viewModel.guestCapacity.value)
            requestModelBuildIt()
        })
        viewModel.bedroomlist.observe(viewLifecycleOwner, Observer {
            it.let {
                viewModel.roomCapacity.set(
                    it?.listSettings!!.get(0)?.startValue!!.toInt().toString()
                )
            }
            requestModelBuildIt()
        })

        viewModel.roomNoCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.roomCapacity.set(viewModel.roomNoCapacity.value)
            requestModelBuildIt()
        })
        viewModel.beds.observe(viewLifecycleOwner, Observer {
            it.let {
                viewModel.bedCapacity.set(
                    it?.listSettings!!.get(0)?.startValue!!.toInt().toString()
                )
            }
            requestModelBuildIt()
        })
        viewModel.bedNoCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.bedCapacity.set(viewModel.bedNoCapacity.value)
            requestModelBuildIt()
        })
        viewModel.typeOfBeds.value.let {
            requestModelBuildIt()
        }
        viewModel.bedType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })

        viewModel.updateCount.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
    }

    fun requestModelBuildIt() {
        if (mBinding.rvStepOne.adapter != null) {
            mBinding.rvStepOne.requestModelBuild()
        }
    }

    private fun setUp() {
        try {
            setChips()
            mBinding.rvStepOne.withModels {

                viewholderHostPlusMinus {
                    id("total guest")
                    if (viewModel.personCapacity1.get().toString().equals("1")) {
                        text(baseActivity!!.getString(R.string.total_guest))
                    } else {
                        text(baseActivity!!.getString(R.string.total_guest))
                    }
                    isBgNeeded(false)
                    paddingTop(false)
                    paddingBottom(true)
                    personCapacity1(viewModel.personCapacity1.get().toString())
                    plusLimit1(viewModel.personCapacity.value!!.listSettings!![0]?.endValue)
                    minusLimit1(viewModel.personCapacity.value!!.listSettings!![0]?.startValue)
                    clickPlus(View.OnClickListener {
                        viewModel.personCapacity1.get()?.let {
                            viewModel.personCapacity1.set(it.toInt().plus(1).toString())
                            viewModel.guestCapacity.value = viewModel.personCapacity1.get()
                            viewModel.becomeHostStep1.value!!.totalGuestCount =
                                viewModel.personCapacity1.get()!!.toInt()
                            viewModel.capacity.value =
                                getString(R.string.For) +viewModel.personCapacity1.get()+ viewModel.personCapacity!!.value!!.listSettings
                                    ?.get(0)!!.otherItemName
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.personCapacity1.get()?.let {
                            viewModel.personCapacity1.set(it.toInt().minus(1).toString())
                            viewModel.guestCapacity.value = viewModel.personCapacity1.get()
                            viewModel.becomeHostStep1.value!!.totalGuestCount =
                                viewModel.personCapacity1.get()!!.toInt()
                            viewModel.capacity.value =
                                getString(R.string.For) +viewModel.personCapacity1.get()+ viewModel.personCapacity!!.value!!.listSettings
                                    ?.get(0)!!.otherItemName
                        }
                    })
                }
                viewholderDivider {
                    id("divider 1")
                }
                viewholderHostPlusMinus {
                    id("total bedrooms")
                    if (viewModel.roomCapacity.get().toString().equals("1")) {
                        text(baseActivity!!.getString(R.string.bedroom_for_guest))
                    } else {
                        text(baseActivity!!.getString(R.string.Bedroom_for_guests))
                    }
                    paddingTop(true)
                    paddingBottom(true)
                    personCapacity1(viewModel.roomCapacity.get().toString())
                    plusLimit1(viewModel.bedroomlist.value!!.listSettings!![0]?.endValue)
                    minusLimit1(viewModel.bedroomlist.value!!.listSettings!![0]?.startValue)
                    clickPlus(View.OnClickListener {
                        viewModel.roomCapacity.get()?.let {
                            viewModel.roomCapacity.set(it.toInt().plus(1).toString())
                            viewModel.roomNoCapacity.value = viewModel.roomCapacity.get()
                            viewModel.becomeHostStep1.value!!.bedroomCount =
                                viewModel.roomCapacity.get()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.roomCapacity.get()?.let {
                            viewModel.roomCapacity.set(it.toInt().minus(1).toString())
                            viewModel.roomNoCapacity.value = viewModel.roomCapacity.get()
                            viewModel.becomeHostStep1.value!!.bedroomCount =
                                viewModel.roomCapacity.get()
                        }
                    })
                }
                viewholderDivider {
                    id("divider 2")
                }
                viewholderHostPlusMinus {
                    id("total beds")
                    if (viewModel.bedCapacity.get().toString().equals("1")) {
                        text(baseActivity!!.getString(R.string.bed_for_guest))
                    } else {
                        text(baseActivity!!.getString(R.string.bed_for_guests))
                    }
                    paddingTop(true)
                    paddingBottom(false)
                    personCapacity1(viewModel.bedCapacity.get().toString())
                    plusLimit1(viewModel.beds.value!!.listSettings!![0]?.endValue)
                    minusLimit1(viewModel.beds.value!!.listSettings!![0]?.startValue)
                    clickPlus(View.OnClickListener {
                        viewModel.bedCapacity.get()?.let {
                            viewModel.bedCapacity.set(it.toInt().plus(1).toString())
                            viewModel.bedNoCapacity.value = viewModel.bedCapacity.get()
                            viewModel.becomeHostStep1.value!!.beds =
                                viewModel.bedCapacity.get()!!.toInt()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.bedCapacity.get()?.let {
                            viewModel.bedCapacity.set(it.toInt().minus(1).toString())
                            viewModel.bedNoCapacity.value = viewModel.bedCapacity.get()
                            viewModel.becomeHostStep1.value!!.beds =
                                viewModel.bedCapacity.get()!!.toInt()
                        }
                    })
                }
                viewholderListTv {
                    id("type of bed")
                    hint(baseActivity!!.getString(R.string.edit_bed))
                    etHeight(false)
                    maxLength(50)
                    onNoticeClick(View.OnClickListener {
                        StepOneOptionsFragment.newInstance("bed").show(childFragmentManager, "bed")
                    })
                }
                viewholderDividerListTv {
                    id("divider 5")
                }
                viewholderHostPlusMinus {
                    id(" bathroom i")
                    paddingTop(true)
                    paddingBottom(false)
                    isBgNeeded(false)
                    if (viewModel.bathroomCapacity.get()!!.toDouble()
                            .equals(1.0) || viewModel.bathroomCapacity.get()!!.toDouble()
                            .equals(0.5)
                        || viewModel.bathroomCapacity.get()!!.toDouble().equals(0.0)
                    ) {
                        text(baseActivity!!.getString(R.string.bathroom))
                    } else {
                        text(baseActivity!!.getString(R.string.bathrooms))
                    }
                    personCapacity1(
                        Utils.formatDecimal(
                            viewModel.bathroomCapacity.get()!!.toDouble()
                        )
                    )
                    plusLimit1(
                        viewModel.noOfBathroom.value?.listSettings?.get(0)?.endValue ?: 0
                    )
                    minusLimit1(
                        viewModel.noOfBathroom.value?.listSettings?.get(0)?.startValue ?: 0
                    )
                    clickPlus(View.OnClickListener {
                        viewModel.bathroomCapacity.get()?.let {
                            viewModel.bathroomCapacity.set(it.toDouble().plus(0.5).toString())
                            viewModel.bathroomCount.value =
                                viewModel.bathroomCapacity.get().toString()
                            viewModel.bathroomCapacity.set(
                                Utils.formatDecimal(
                                    viewModel.bathroomCapacity.get()!!.toDouble()
                                )
                            )
                            viewModel.becomeHostStep1.value!!.bathroomCount =
                                viewModel.bathroomCapacity.get()!!.toDouble()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.bathroomCapacity.get()?.let {
                            viewModel.bathroomCapacity.set(it.toDouble().minus(0.5).toString())
                            viewModel.bathroomCount.value =
                                viewModel.bathroomCapacity.get().toString()
                            viewModel.bathroomCapacity.set(
                                Utils.formatDecimal(
                                    viewModel.bathroomCapacity.get()!!.toDouble()
                                )
                            )
                            viewModel.becomeHostStep1.value!!.bathroomCount =
                                viewModel.bathroomCapacity.get()!!.toDouble()
                        }
                    })
                }
                viewholderListTv {
                    id("bathroom type")
                    etHeight(false)
                    maxLength(50)
                    hint(viewModel.bathroomType.value)
                    onNoticeClick(View.OnClickListener {
                        StepOneOptionsFragment.newInstance("bathroomOptions")
                            .show(childFragmentManager, "bathroomOptions")
                    })
                }

            }
        } catch (E: Exception) {
            showError()
        }
    }

    private fun setChips() {
        mBinding.apply {
            placeType = false
            noOfGuests = true
            bedrooms = false
            baths = false
            address = false
            location = false
            amenities = false
            safety = false
            space = false
            placeTypeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            })
            noOfGuestsClick = (View.OnClickListener {

            })
            bedroomsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.TYPE_OF_BEDS)
            })
            bathsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.NO_OF_BATHROOM)
            })
            addressClick = (View.OnClickListener {
                viewModel?.navigator?.navigateScreen(StepOneViewModel.NextScreen.ADDRESS)
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
