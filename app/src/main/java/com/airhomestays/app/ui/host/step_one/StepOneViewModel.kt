package com.airhomestays.app.ui.host.step_one

import android.os.Handler
import android.os.Looper
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import com.airhomestays.app.Constants
import com.airhomestays.app.CreateListingMutation
import com.airhomestays.app.GetCountrycodeQuery
import com.airhomestays.app.GetListingSettingQuery
import com.airhomestays.app.GetStep1ListingDetailsQuery
import com.airhomestays.app.ManageListingStepsMutation
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.BecomeHostStep1
import com.airhomestays.app.vo.PersonCount
import org.json.JSONArray
import timber.log.Timber
import java.net.URLEncoder
import java.text.DecimalFormat
import javax.inject.Inject

class StepOneViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<StepOneNavigator>(dataManager, resourceProvider) {
    var currentFragment: Fragment? = null
    var fragmentTag: String? = ""

    var isEdit = false
    var isMapMove = true
    var isMapMoved = MutableLiveData(false)

    var totalBedCount = 0
    var editBedCount = 0

    var uiMode: Int? = null
    var backPressWelcome: Int? = null


    val msg = ObservableField("")
    var selectedAmenities = ArrayList<Int>()
    var selectedDetectors = ArrayList<Int>()
    var selectedSpace = ArrayList<Int>()
    var selectedBeds = ArrayList<Int>()
    var address = ObservableField<String>()
    var isAddressResolved = false
    lateinit var defaultSettings: MutableLiveData<GetListingSettingQuery.Results?>

    val roomtypelist = MutableLiveData<GetListingSettingQuery.RoomType?>()
    val roomType = MutableLiveData<String>()
    var p1 = MutableLiveData<LatLng>()
    val personCapacity = MutableLiveData<GetListingSettingQuery.PersonCapacity?>()
    val capacity = MutableLiveData<String?>()

    val housetypelist = MutableLiveData<GetListingSettingQuery.HouseType?>()
    val houseType = MutableLiveData<String?>()

    val roomSizelist = MutableLiveData<GetListingSettingQuery.BuildingSize?>()
    val roomSizeType = MutableLiveData<String>()
    var validlocation = MutableLiveData<Boolean>(true)

    val yesNoType = MutableLiveData<String>()
    val yesNoString = ObservableField<String>()
    val personCapacity1 = ObservableField<String>()
    val guestCapacity = MutableLiveData<String>()
    val roomCapacity = ObservableField<String>()
    val roomNoCapacity = MutableLiveData<String>()
    val bedCapacity = ObservableField<String>()
    val bedNoCapacity = MutableLiveData<String>()
    val guestPlaceType = MutableLiveData<String>()

    var defaultselectedBedTypeId = ArrayList<String>()
    val bedroomlist = MutableLiveData<GetListingSettingQuery.Bedrooms?>()
    val bedTypelist = MutableLiveData<GetListingSettingQuery.BedType?>()
    val bedType = MutableLiveData<String>()
    var typeOfBeds = MutableLiveData<ArrayList<PersonCount>>()

    val bathroomlist = MutableLiveData<GetListingSettingQuery.BathroomType?>()
    val bathroomType = MutableLiveData<String>()

    val bathroomCapacity = ObservableField<String>()
    val noOfBathroom = MutableLiveData<GetListingSettingQuery.Bathrooms?>()
    val bathroomCount = MutableLiveData<String>()
    val updateCount = MutableLiveData<ArrayList<String>>()
    val beds = MutableLiveData<GetListingSettingQuery.Beds?>()
    val bedCount = MutableLiveData<GetListingSettingQuery.Beds?>()
    val noOfBed = MutableLiveData<String>()

    val list = MutableLiveData<List<GetCountrycodeQuery.Result?>?>()
    val listSearch = MutableLiveData<ArrayList<GetCountrycodeQuery.Result?>?>()
    val isCountryCodeLoad = ObservableField(false)

    val amenitiedId = MutableLiveData<ArrayList<Int>>()
    val aafetyAmenitiedId = MutableLiveData<ArrayList<Int>>()
    val profilep = dataManager.currentUserProfilePicUrl
    val spacesId = MutableLiveData<ArrayList<Int>>()
    val bedTypesId = MutableLiveData<ArrayList<Int>>()
    val amenitiesList = MutableLiveData<GetListingSettingQuery.Amenities?>()
    val safetyAmenitiesList = MutableLiveData<GetListingSettingQuery.SafetyAmenities?>()
    val sharedSpaceList = MutableLiveData<GetListingSettingQuery.Spaces?>()

    val street = ObservableField("")
    val country = ObservableField("")
    val countryCode = ObservableField("")
    val buildingName = ObservableField("")
    val city = ObservableField("")
    val state = ObservableField("")
    val zipcode = ObservableField("")
    val mobileNumber = ObservableField("")
    val responseFromApi = ObservableField("")
    var lat = ObservableField("")
    var lng = ObservableField("")
    val latLng = ObservableField("")
    val listId = ObservableField("")
    val location = ObservableField("")
    var showAmentiesId: Int? = null
    var showSafetyAmentiesId: Int? = null
    var showSpacesId: Int? = null
    var showbedTypesId: Int? = null
    var isListAdded = false
    var retryCalled = ""
    val lottieProgress =
        ObservableField<StepOneViewModel.LottieProgress>(StepOneViewModel.LottieProgress.LOADING)
    val isNext = ObservableField<Boolean>(false)
    val isAddressFilled = ObservableField<Boolean>(false)
    var requestQueue: RequestQueue? = null
    var listSetting = MutableLiveData<GetListingSettingQuery.Results?>()

    var becomeHostStep1 = MutableLiveData<BecomeHostStep1>()
    var bedTypesArray = ArrayList<HashMap<String, String>>()

    enum class NextScreen {
        KIND_OF_PLACE,
        NO_OF_GUEST,
        TYPE_OF_BEDS,
        NO_OF_BATHROOM,
        ADDRESS,
        MAP_LOCATION,
        AMENITIES,
        SAFETY_PRIVACY,
        GUEST_SPACE,
        FINISHED,
        SAVE_N_EXIT,
        SELECT_COUNTRY,
        TYPE_OF_SPACE
    }

    enum class BackScreen {
        KIND_OF_PLACE,
        NO_OF_GUEST,
        TYPE_OF_BEDS,
        NO_OF_BATHROOM,
        ADDRESS,
        MAP_LOCATION,
        TYPE_OF_SPACE,
        AMENITIES,
        SAFETY_PRIVACY,
        GUEST_SPACE,
        WELCOME
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    fun loadDefaultSettings(): MutableLiveData<GetListingSettingQuery.Results?> {
        if (!::defaultSettings.isInitialized) {
            defaultSettings = MutableLiveData()
            becomeHostStep1 = MutableLiveData()
            getListingSetting("add")
        }
        return defaultSettings
    }

    fun onContinueClick(screen: NextScreen) {
        navigator.navigateScreen(screen)
    }

    fun showError() {
        navigator.showSnackbar(
            resourceProvider.getString(R.string.error_1),
            resourceProvider.getString(R.string.currently_offline)
        )
    }

    fun checkValidation() {
        isMapMove = true
        navigator.hideKeyboard()
        address.set("")
        location.set("")
        if (becomeHostStep1.value?.street == street.get() && becomeHostStep1.value?.country == countryCode.get() && becomeHostStep1.value?.state == state.get()) {
            location.set("${becomeHostStep1.value!!.lat},${becomeHostStep1.value!!.lng}")
        } else {

            address.set(
                street.get()!!.trim() + ", " + city.get()!!.trim() + ", " + state.get()!!
                    .trim() + ", " + countryCode.get()!!
                    .trim() + ", " + zipcode.get()!!.trim()
            )

            location.set(getLocationFromGoogle(address.get()!!))
            isMapMoved.value = false
            becomeHostStep1.value?.street = street.get()!!
            becomeHostStep1.value?.country = countryCode.get()!!
            becomeHostStep1.value?.state = state.get()!!
        }

        if (country.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.error_1),
                resourceProvider.getString(R.string.please_enter_country)
            )
        } else if (street.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.error_1),
                resourceProvider.getString(R.string.please_enter_street)
            )
        } else if (city.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.error_1),
                resourceProvider.getString(R.string.please_enter_city)
            )
        } else if (state.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.error_1),
                resourceProvider.getString(R.string.please_enter_state)
            )
        } else if (location.get().isNullOrEmpty() && isEdit) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.error_1),
                resourceProvider.getString(R.string.incorrect_location)
            )
        } else {
            if (buildingName.get().isNullOrEmpty()) {
                buildingName.set("")
            }
            navigator.hideSnackbar()
            if (location.get().isNullOrEmpty()) {
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.error_1),
                    resourceProvider.getString(R.string.incorrect_location)
                )
            } else {
                validlocation.observeForever {
                    Handler(Looper.getMainLooper()).postDelayed(
                        Runnable {
                            navigator.hideSnackbar()
                            if (listId.get()!!.isEmpty() || listId.get()!!.isBlank()) {
                                if (validlocation.value!!.equals(true)) {
                                    becomeHostStep1.value!!.country = country.get()!!
                                    becomeHostStep1.value!!.street = street.get()!!.trim()
                                    becomeHostStep1.value!!.buildingName =
                                        buildingName.get()!!.trim()
                                    becomeHostStep1.value!!.state = state.get()!!.trim()
                                    becomeHostStep1.value!!.zipcode = zipcode.get()!!.trim()

                                    isNext.set(true)
                                    lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
                                    if (isMapMove) {
                                        onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                                    }

                                    isMapMove = false


                                }
                            } else {

                                if (validlocation.value!!.equals(true)) {
                                    isNext.set(true)
                                    lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
                                    isNext.set(false)
                                    if (isMapMove) {
                                        onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                                    }
                                    isMapMove = false
                                }
                            }
                        }, 900
                    )
                }


            }
        }
    }

    fun getLocationFromGoogle(address: String): String {
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${
            URLEncoder.encode(
                address,
                "UTF-8"
            )
        }&key=${Constants.googleMapKey}"
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->

            try {
                isNext.set(false)
                lottieProgress.set(LottieProgress.NORMAL)
                val jsonArray = response.getJSONArray("results")
                responseFromApi.set(jsonArray.toString())

                if (jsonArray.length() > 0) {
                    val jsonObject = jsonArray.getJSONObject(0)


                    val geometry = jsonObject.getJSONObject("geometry")
                    val locations = geometry.getJSONObject("location")
                    lat.set(locations.getDouble("lat").toString())
                    lng.set(locations.getDouble("lng").toString())
                    p1.value = LatLng(lat.get()!!.toDouble(), lng.get()!!.toDouble())
                    validlocation.postValue(true)


                } else {
                    isNext.set(false)
                    lottieProgress.set(LottieProgress.NORMAL)
                    navigator.showToast("query limit exceed")
                    validlocation.postValue(false)
                    Timber.d("Enable geoCoding api or check the billing account enable in google cloud console")
                }
            } catch (e: Exception) {
                navigator.showToast("Something went wrong try again later")
            }
        }, { error ->
            isNext.set(false)
            responseFromApi.set(error.message)
            lottieProgress.set(LottieProgress.NORMAL)
            navigator.showToast("Something went wrong")
            error.printStackTrace()
        })
        requestQueue?.add(request)
        return "$lat,$lng"
    }

    fun updateStep1(locationLatLng: String, goto: Boolean) {
        locationLatLng.let {
            if (locationLatLng.isNullOrEmpty() && isEdit) {
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.error_1),
                    resourceProvider.getString(R.string.incorrect_location)
                )
            } else {
                if (buildingName.get().isNullOrEmpty()) {
                    buildingName.set("")
                }
                navigator.hideSnackbar()
                if (locationLatLng.isNullOrEmpty()) {
                    navigator.showSnackbar(
                        resourceProvider.getString(R.string.error_1),
                        resourceProvider.getString(R.string.incorrect_location)
                    )
                } else {
                    if (goto) {
                        onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                    } else {
                        navigator.hideSnackbar()
                        if (listId.get()!!.isEmpty() || listId.get()!!.isBlank()) {
                            isNext.set(true)
                            location.set(locationLatLng.toString())
                            lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
                            createHostStepOne()
                        } else {
                            isNext.set(true)
                            lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
                            isNext.set(false)
                            lottieProgress.set(StepOneViewModel.LottieProgress.NORMAL)
                            location.set(locationLatLng.toString())
                            onContinueClick(StepOneViewModel.NextScreen.KIND_OF_PLACE)
                        }
                    }

                }
            }
        }
    }

    fun createHostStepOne() {

        updateCount.value!!.forEachIndexed { index, s ->
            if (s.equals("0").not()) {
                val temp = HashMap<String, String>()
                temp.put("bedCount", s)
                temp.put("bedType", defaultselectedBedTypeId.get(index))
                bedTypesArray.add(temp)
            }
        }

        val jsonVal = JSONArray(bedTypesArray)

        var bath = becomeHostStep1.value!!.bathroomCount!!.toString()
        if (bath.contains(",")) {
            bath = bath.replace(",", ".")
        }
        val request = CreateListingMutation(
            roomType = becomeHostStep1.value!!.placeType.toOptional(),
            personCapacity = becomeHostStep1.value!!.totalGuestCount!!.toInt().toOptional(),
            residenceType =becomeHostStep1.value!!.yesNoOptions.toOptional(),
            buildingSize =becomeHostStep1.value!!.roomCapacity.toOptional(),
            bathroomType =becomeHostStep1.value!!.bathroomSpace.toOptional(),
            bathrooms =bath.toDouble().toOptional(),
            beds = becomeHostStep1.value!!.beds!!.toInt().toOptional(),
            bedTypes = jsonVal.toString().toOptional(),
            bedType = "17".toOptional(),
            bedrooms = becomeHostStep1.value!!.bedroomCount.toOptional(),
            city = city.get().toOptional(),
            amenities = selectedAmenities.toList().toOptional(),
            houseType = becomeHostStep1.value!!.houseType.toOptional(),
            safetyAmenities = selectedDetectors.toList().toOptional(),
            spaces = selectedSpace.toList().toOptional(),
            state = state.get().toOptional(),
            country = countryCode.get().toOptional(),
            zipcode = zipcode.get().toOptional(),
            street = street.get().toOptional(),
            buildingName = buildingName.get().toOptional(),
            isMapTouched = true.toOptional(),
            mobileNumber = mobileNumber.get().toOptional()
        )

        compositeDisposable.add(dataManager.doCreateListing(request)
            .doOnSubscribe {
            }
            .doFinally {
            }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                { response ->
                    val data = response.data
                    try {
                        if (data?.createListing?.status == 200) {
                            retryCalled = ""
                            val actionType = data?.createListing!!.actionType
                            if (actionType.equals("create")) {
                                retryCalled = ""
                                listId.set(data.createListing!!.id.toString())
                                amenitiedId.value = selectedAmenities
                                spacesId.value = selectedSpace
                                aafetyAmenitiedId.value = selectedDetectors
                                bedTypesId.value = selectedBeds
                                manageSteps()
                                isNext.set(false)
                                lottieProgress.set(StepOneViewModel.LottieProgress.NORMAL)
                                onContinueClick(StepOneViewModel.NextScreen.AMENITIES)
                            }
                        } else if (data?.createListing!!.status == 500) {
                            navigator.openSessionExpire("")
                        } else {
                            if (data?.createListing!!.errorMessage == null)
                                navigator.showError()
                            else navigator.showToast(
                                data?.createListing!!.errorMessage.toString()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError(e)
                    }
                },
                {
                    navigator.showSnackbar(
                        resourceProvider.getString(R.string.error),
                        resourceProvider.getString(R.string.currently_offline)
                    )
                }
            ))
    }

    fun updateHostStepOne(flag: Boolean = false) {
        try {
            bedTypesArray.clear()
            val lati = lat.get().toString()
            val longi = lng.get().toString()
            updateCount.value!!.forEachIndexed { index, s ->
                if (s.equals("0").not()) {
                    val temp = HashMap<String, String>()
                    temp.put("bedCount", s)
                    temp.put("bedType", defaultselectedBedTypeId.get(index))
                    bedTypesArray.add(temp)
                }
            }
            var building = ""
            if (buildingName.get().isNullOrEmpty().not()) {
                building = buildingName.get()!!
            }
            var bath = becomeHostStep1.value!!.bathroomCount!!.toString()
            if (bath.contains(",")) {
                bath = bath.replace(",", ".")
            }
            val jsonVal = JSONArray(bedTypesArray)
            val request = CreateListingMutation(
                roomType = becomeHostStep1.value!!.placeType.toOptional(),
                personCapacity = becomeHostStep1.value!!.totalGuestCount!!.toInt().toOptional(),
                residenceType =becomeHostStep1.value!!.yesNoOptions.toOptional(),
                buildingSize =becomeHostStep1.value!!.roomCapacity.toOptional(),
                bathroomType =becomeHostStep1.value!!.bathroomSpace.toOptional(),
                bathrooms =bath.toDouble().toOptional(),
                beds = becomeHostStep1.value!!.beds!!.toInt().toOptional(),
                bedTypes = jsonVal.toString().toOptional(),
                bedType = "17".toOptional(),
                bedrooms = becomeHostStep1.value!!.bedroomCount.toOptional(),
                city = city.get().toOptional(),
                amenities = selectedAmenities.toList().toOptional(),
                houseType = becomeHostStep1.value!!.houseType.toOptional(),
                safetyAmenities = selectedDetectors.toList().toOptional(),
                spaces = selectedSpace.toList().toOptional(),
                state = state.get().toOptional(),
                country = countryCode.get().toOptional(),
                zipcode = zipcode.get().toOptional(),
                street = street.get().toOptional(),
                buildingName = building.toOptional(),
                lat = lati.toDouble().toOptional(),
                lng = longi.toDouble().toOptional(),
                listId =listId.get()!!.toInt().toOptional(),
                isMapTouched = true.toOptional(),
                mobileNumber = mobileNumber.get().toOptional()
            )

            compositeDisposable.add(dataManager.doCreateListing(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                    { response ->
                        val data = response.data
                        try {
                            if (data?.createListing?.status == 200) {
                                val actionType = data?.createListing!!.actionType
                                retryCalled = ""
                                if (actionType.equals("update")) {
                                    amenitiedId.value = selectedAmenities
                                    spacesId.value = selectedSpace
                                    aafetyAmenitiedId.value = selectedDetectors
                                    bedTypesId.value = selectedBeds
                                    lat.set(lati)
                                    lng.set(longi)
                                    if (flag) {
                                        manageSteps(flag)
                                    } else {
                                        navigator.navigateScreen(NextScreen.FINISHED)
                                    }
                                }

                            } else if (data?.createListing!!.status == 500) {
                                navigator.openSessionExpire("")
                            } else {
                                navigator.showSnackbar(
                                    resourceProvider.getString(R.string.error),
                                    response?.data?.createListing?.errorMessage.toString()
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError(e)
                        }
                    },
                    {
                        handleException(it)
                    }
                ))
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError(e)
        }
    }

    fun getListingSetting(from: String) {
        val request = GetListingSettingQuery()

        compositeDisposable.add(dataManager.doGetListingSettings(request)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                { response ->
                    val data = response.data
                    try {
                        if (data?.getListingSettings?.status == 200) {
                            val result = data!!.getListingSettings!!.results
                            listSetting.value = result
                            if (from.equals("edit")) {
                                personCapacity.value = result!!.personCapacity
                                roomtypelist.value = result!!.roomType
                                housetypelist.value = result!!.houseType
                                roomSizelist.value = result!!.buildingSize
                                bedTypelist.value = result!!.bedType
                                bedroomlist.value = result.bedrooms
                                beds.value = result!!.beds
                                bedCount.value = result!!.beds
                                noOfBathroom.value = result!!.bathrooms
                                bathroomlist.value = result!!.bathroomType
                                amenitiesList.value = result!!.amenities
                                safetyAmenitiesList.value = result!!.safetyAmenities
                                sharedSpaceList.value = result!!.spaces
                                yesNoType.value = yesNoString.get()
                            } else {
                                defaultSettings.value = result
                                roomtypelist.value = result!!.roomType
                                roomType.value =
                                    result!!.roomType!!.listSettings!![0]?.itemName!!
                                capacity.value =
                                    resourceProvider.getString(R.string.For) + " 1 " + result.personCapacity!!
                                        .listSettings!![0]?.itemName!!
                                personCapacity.value = result!!.personCapacity
                                housetypelist.value = result!!.houseType
                                houseType.value =
                                    result!!.houseType!!.listSettings!![0]?.itemName!!
                                guestPlaceType.value =
                                    result!!.roomType!!.listSettings!![0]?.itemName!!
                                roomSizelist.value = result!!.buildingSize
                                roomSizeType.value =
                                    result!!.buildingSize!!.listSettings!![0]?.itemName!!
                                yesNoType.value = "Yes"
                                beds.value = result!!.beds
                                bedroomlist.value = result!!.bedrooms
                                bedTypelist.value = result!!.bedType
                                bathroomlist.value = result!!.bathroomType
                                noOfBathroom.value = result!!.bathrooms
                                bathroomType.value =
                                    result!!.bathroomType!!.listSettings!![0]?.itemName!!
                                bedCount.value = result!!.beds
                                noOfBed.value = result!!.beds!!.listSettings!![0]?.itemName!!
                                amenitiesList.value = result!!.amenities
                                safetyAmenitiesList.value = result!!.safetyAmenities
                                sharedSpaceList.value = result!!.spaces
                                setStepOne(result)
                            }
                            setData(result!!.bedType, "add")
                            if (from.equals("edit")) {
                                getStep1ListingDetails()
                            } else {
                                getCountryCode()
                            }

                        } else if (data?.getListingSettings!!.status == 500) {
                            navigator.openSessionExpire("")
                        } else {
                            if (data.getListingSettings!!.errorMessage == null)
                                navigator.showError()
                            else navigator.showToast(
                                data.getListingSettings!!.errorMessage.toString()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError(e)
                    }
                },
                {
                    handleException(it)
                }
            ))
    }

    private fun setStepOne(result: GetListingSettingQuery.Results?) {
        becomeHostStep1.value = BecomeHostStep1(
            placeType = result!!.roomType!!.listSettings!![0]?.id.toString()!!,
            guestCapacity = result!!.personCapacity!!.listSettings!![0]?.id.toString()!!,
            houseType = result!!.houseType!!.listSettings!![0]?.id.toString()!!,
            guestSpace = result!!.roomType!!.listSettings!![0]?.id.toString()!!,
            roomCapacity = result!!.buildingSize!!.listSettings!![0]?.id.toString()!!,
            yesNoOptions = yesNoString!!.get(),
            totalGuestCount = 1,
            bedroomCount = "1",
            bedCount = 1,
            bedType = "",
            bedTypes = "",
            beds = 1,
            bathroomCount = 1.0,
            bathroomSpace = result!!.bathroomType!!.listSettings!![0]?.id.toString()!!,
            country = "",
            street = "",
            state = "",
            zipcode = "",
            isMapTouched = false,
            buildingName = "",
            lat = 0.0,
            lng = 0.0,
            amentiesSelected = arrayListOf(),
            safetyAmentiesSelected = arrayListOf(),
            mobileNumber = "",
            guestSpacesSelected = arrayListOf()
        )
    }

    fun getStep1ListingDetails() {
        val format = DecimalFormat("0.#")
        val buildQuery = GetStep1ListingDetailsQuery(
            listId = listId.get().toString(),
            preview = true.toOptional()
        )
        compositeDisposable.add(dataManager.doGetStep1ListingDetailsQuery(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getListingDetails
                    if (data?.status == 200) {
                        isListAdded = true
                        retryCalled = ""
                        var bathroom = data.results!!.bathrooms
                        try {
                            var roomtype = ""
                            var houseType = ""
                            if (data.results!!.settingsData!!.size > 0) {
                                if (data.results!!.settingsData!![0]?.listsettings == null) {
                                    roomtype =
                                        listSetting.value?.roomType!!.listSettings!![0]?.id
                                            .toString()!!
                                } else {
                                    roomtype = data.results!!.settingsData!![0]?.listsettings!!
                                        .id.toString()
                                }
                            }
                            becomeHostStep1.value = BecomeHostStep1(
                                placeType = roomtype,
                                guestCapacity = data.results!!.personCapacity.toString(),
                                houseType = data.results!!.settingsData!![1]?.listsettings!!
                                    .id.toString(),
                                guestSpace = roomtype,
                                roomCapacity = data.results!!.settingsData!![2]?.listsettings!!
                                    .id.toString(),
                                yesNoOptions = data.results!!.residenceType.toString(),
                                totalGuestCount = data.results!!.personCapacity,
                                bedroomCount = data.results!!.bedrooms.toString(),
                                beds = data.results!!.beds,
                                bedCount = data.results!!.beds,
                                bedType = "",
                                bedTypes = arrayListOf(showbedTypesId).toString(),
                                bathroomCount = bathroom,
                                bathroomSpace = data.results!!
                                    .settingsData!![3]?.listsettings!!.id.toString(),
                                country = data.results!!.country.toString(),
                                street = data.results!!.street.toString(),
                                state = data.results!!.state.toString(),
                                zipcode = data.results!!.zipcode!!,
                                isMapTouched = true,
                                buildingName = data.results!!.buildingName.toString(),
                                lat = data.results!!.lat,
                                lng = data.results!!.lng,
                                amentiesSelected = arrayListOf(showAmentiesId),
                                safetyAmentiesSelected = arrayListOf(showSafetyAmentiesId),
                                mobileNumber = "",
                                guestSpacesSelected = arrayListOf(showSpacesId)
                            )

                            setPrefilledData(data)
                            setCountry(data.results!!.country.toString())
                            if (currentFragment == null)
                                navigator.navigateScreen(NextScreen.KIND_OF_PLACE)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else if (data?.status == 500) {
                        isListAdded = false
                        navigator.openSessionExpire("")
                    } else {
                        isListAdded = false
                        navigator.show404Page()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError(e)
                }
            }, {

                it.printStackTrace()
                handleException(it)
            })
        )
    }

    fun setCountry(county: String) {
        getCountryCode()
    }

    private fun setPrefilledData(data: GetStep1ListingDetailsQuery.GetListingDetails) {
        try {
            var bathroom = data.results!!.bathrooms!!.toDouble().toString()
            if (bathroom.contains(",")) {
                bathroom = bathroom.replace(",", ".")
            }
            var roomtype = ""
            if (data.results!!.settingsData!![0]?.listsettings == null) {
                roomtype = listSetting.value?.roomType!!.listSettings!![0]?.itemName!!
            } else {
                roomtype = data.results!!.settingsData!![0]?.listsettings!!.itemName!!
            }
            roomType.value = roomtype
            capacity.value = data.results!!.personCapacity.toString()
            houseType.value =
                data.results!!.settingsData!![1]?.listsettings?.itemName.toString()
            roomSizeType.value =
                data.results!!.settingsData!![2]?.listsettings?.itemName.toString()
            guestCapacity.value = data.results!!.personCapacity.toString()
            roomNoCapacity.value = data.results!!.bedrooms.toString()
            bedNoCapacity.value = data.results!!.beds.toString()
            bathroomCount.value = bathroom
            bathroomType.value =
                data.results!!.settingsData!![3]?.listsettings!!.itemName.toString()
            country.set(data.results!!.country.toString())
            countryCode.set(data.results!!.country.toString())
            city.set(data.results!!.city.toString())
            if (data.results!!.residenceType.equals("1")) {
                yesNoString.set("Yes")
                yesNoType.value = yesNoString.get()
            } else {
                yesNoString.set("No")
                yesNoType.value = yesNoString.get()
            }
            if (data.results!!.buildingName.toString().isNullOrEmpty()) {
                buildingName.set("")
            } else {
                buildingName.set(data.results!!.buildingName.toString())
            }
            street.set(data.results!!.street.toString())
            state.set(data.results!!.state.toString())
            zipcode.set(data.results!!.zipcode!!)
            lat.set(data.results!!.lat.toString())
            lng.set(data.results!!.lng.toString())
            mobileNumber.set(data.results!!.mobileNumber)
            if (data!!.results!!.buildingName.isNullOrEmpty()) {
                buildingName.set("")
            }
            data!!.results!!.userAmenities!!.forEachIndexed { i, userAmenity ->
                showAmentiesId = data!!.results!!.userAmenities!![i]?.id
                showAmentiesId?.let { selectedAmenities.add(it) }
            }
            data!!.results!!.userSafetyAmenities!!.forEachIndexed { i, safetyAmenity ->
                showSafetyAmentiesId = data!!.results!!.userSafetyAmenities!![i]?.id
                showSafetyAmentiesId?.let { selectedDetectors.add(it) }
            }
            data!!.results!!.userSpaces!!.forEachIndexed { i, userSpace ->
                showSpacesId = data!!.results!!.userSpaces!![i]?.id
                showSpacesId?.let { selectedSpace.add(it) }
            }
            var userBedsAPI = data.results!!.userBedsTypes
            defaultselectedBedTypeId.forEachIndexed { index, s ->
                userBedsAPI?.forEachIndexed { i, userBedsType ->
                    if (s.equals(userBedsType?.bedType.toString())) {
                        updateCount.value!!.removeAt(index)
                        updateCount.value!!.add(index, userBedsType?.bedCount.toString())
                    }
                }
            }
            updateCount.value?.forEachIndexed { index, s ->
                editBedCount = editBedCount + s.toInt()
            }
            totalBedCount = editBedCount
        } catch (e: Exception) {
            navigator.showError(e)
        }
    }

    fun manageSteps(flag: Boolean = false) {
        val buildQuery = ManageListingStepsMutation(
            listId =listId.get().toString(),
            currentStep =1
            )

        compositeDisposable.add(dataManager.doManageListingSteps(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.manageListingSteps
                    if (data!!.status == 200) {
                        if (flag) {
                            navigator.navigateScreen(NextScreen.FINISHED)
                        }
                    } else if (data.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        data.errorMessage?.let {
                            navigator.showToast(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError(e)
                }
            }, {
                it.printStackTrace()
                handleException(it)
            })
        )
    }


    private fun setData(bedType: GetListingSettingQuery.BedType?, from: String) {
        val list = ArrayList<PersonCount>()
        val countList = ArrayList<String>()

        for (i in 0 until bedType!!.listSettings!!.size) {
            list.add(
                PersonCount(
                    itemId = bedType!!.listSettings!![i]?.id!!,
                    itemName = bedType!!.listSettings!![i]?.itemName!!,
                    startValue = bedCount.value!!.listSettings!![0]?.startValue!!,
                    endValue = bedCount.value!!.listSettings!![0]?.endValue!!,
                    updatedCount = 0
                )
            )

            countList.add("0")
            defaultselectedBedTypeId.add(bedType!!.listSettings!![i]?.id.toString()!!)
        }
        updateCount.value = countList
        typeOfBeds.value = list

        if (from.equals("add")) {
            updateCount.value = countList
        }
    }

    fun getCountryCode() {
        val query = GetCountrycodeQuery()
        compositeDisposable.add(dataManager.getCountryCode(query)
            .doOnSubscribe { }
            .doFinally { }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getCountries
                    if (data?.status == 200) {
                        isCountryCodeLoad.set(true)
                        list.value = response.data!!.getCountries!!.results

                    } else if (data!!.status == 500) {
                        (navigator as BaseNavigator).openSessionExpire("")
                    } else {
                        isCountryCodeLoad.set(false)
                        if (data.errorMessage == null)
                            navigator.showError()
                        else navigator.showToast(data.errorMessage.toString())
                    }
                } catch (e: Exception) {
                    isCountryCodeLoad.set(false)
                    e.printStackTrace()
                    navigator.showError(e)
                }
            }, {
                isCountryCodeLoad.set(false)
                handleException(it)
            })
        )
    }

    fun step1Retry(strUser: String?) {
        if (strUser.isNullOrEmpty()) {
            getListingSetting("add")
        } else {
            getListingSetting("edit")
        }

    }

    fun onSearchTextChanged(text: CharSequence) {
        if (text.isNotEmpty()) {
            val searchText = text.toString().capitalize()
            val containsItem = ArrayList<GetCountrycodeQuery.Result?>()
            list.value?.forEachIndexed { _, result ->
                result?.countryName?.let {
                    if (it.contains(searchText)) {
                        containsItem.add(result)
                    }
                }
            }
            listSearch.value = containsItem
        } else {
            list.value?.let {
                listSearch.value = ArrayList(it)
            }
        }
    }
}