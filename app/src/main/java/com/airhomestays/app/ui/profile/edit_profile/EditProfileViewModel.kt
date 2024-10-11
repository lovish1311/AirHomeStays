package com.airhomestays.app.ui.profile.edit_profile

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.airhomestays.app.CodeVerificationMutation
import com.airhomestays.app.Constants
import com.airhomestays.app.EditProfileMutation
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.R
import com.airhomestays.app.SendConfirmEmailQuery
import com.airhomestays.app.SocialLoginVerifyMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.ProfileDetails
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<EditProfileNavigator>(dataManager,resourceProvider) {

    val firstName = ObservableField("")
    val lastName = ObservableField("")
    val aboutMe = ObservableField("")
    val gender = ObservableField("")
    val genderLanguage = ObservableField("")
    val pic = ObservableField("")
    val dob = ObservableField("")
    val email = ObservableField("")
    val phone = ObservableField("")
    val languages = ObservableField("")
    val languagesValue = ObservableField("")
    val location = ObservableField("")
    val currency = ObservableField("")
    val currencySymbol = ObservableField("")
    val currencyWithSymbol = ObservableField("")
    val createdAt = ObservableField("")
    val emailVerified = ObservableField<Boolean>()
    val fbVerified = ObservableField<Boolean>()
    val googleVerified = MutableLiveData<Boolean>(false)
    val fbVerify = MutableLiveData<Boolean>(false)
    val emailverify = MutableLiveData<Boolean?>(false)
    val phVerified = ObservableField<Boolean>()
    val isProgressLoading = ObservableBoolean(false)

    val temp = ObservableField("")
    val temp1 = ObservableField("")

    val layoutId = ObservableField(0)

    val dob1 = ObservableField<Array<Int>>()

    fun done() {
        if (checkIdAndToken()) {
            when (layoutId.get()) {
                R.layout.include_edit_email -> { checkEmail() }
                R.layout.include_edit_phone -> { checkPhone() }
                R.layout.include_edit_location -> { checkLocation() }
                R.layout.include_edit_aboutme -> { checkAboutMe() }
                R.layout.include_edit_name -> { checkName() }
            }
        } else {
            navigator.showError()
        }
    }

    private fun checkIdAndToken(): Boolean {
        return !(dataManager.firebaseToken.isNullOrEmpty() && dataManager.currentUserId.isNullOrEmpty())
    }

    private fun checkName() {
        if (temp.get().toString().trim().isNotEmpty() && temp1.get().toString().trim().isNotEmpty()) {
            val strings = arrayOf(temp.get().toString().trim(), temp1.get().toString().trim())
            updateProfile("firstName", Gson().toJson(strings))
            navigator.moveToBackScreen()
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_name),
                    resourceProvider.getString(R.string.invalid_name_desc))
        }
    }

    fun checkName(firstName :String,lastName :String) {
        if (firstName.trim().isNotEmpty() && lastName.trim().isNotEmpty()) {
            val strings = arrayOf(firstName.trim(), lastName.trim())
            updateProfile("firstName", Gson().toJson(strings))
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_name),
                    resourceProvider.getString(R.string.invalid_name_desc))
        }
    }

    private fun checkEmail() {
        if (Utils.isValidEmail(temp.get().toString().trim())) {
            updateProfile("email", temp.get().toString().trim())
            navigator.moveToBackScreen()
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_email),
                    resourceProvider.getString(R.string.invalid_email_desc))
        }
    }

    private fun checkPhone() {
        if (temp.get().toString().trim().length > 6) {
            updateProfile("phoneNumber", temp.get().toString().trim())
            navigator.moveToBackScreen()
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_phone),
                    resourceProvider.getString(R.string.invalid_phone_desc))
        }
    }

     fun checkLocation() {
        if(location.get()!!.trim().isNotEmpty()){
            updateProfile("location", location.get().toString().trim())
        }

    }

    fun callCheckAboutMe(){
        checkAboutMe()
    }

    private fun checkAboutMe() {
        if(temp.get()!!.trim().isNotEmpty()){
            updateProfile("info", temp.get().toString().trim())
            navigator.moveToBackScreen()
        }else{
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_user_info),
                    resourceProvider.getString(R.string.invalid_info))
        }

    }

    fun updateProfile(fieldName: String, fieldValue: String) {
        try {
            val mutate = EditProfileMutation(
                    fieldName = fieldName,
                    fieldValue = fieldValue.toOptional(),
                    deviceId = dataManager.firebaseToken!!,
                    userId = dataManager.currentUserId!!,
                    deviceType = Constants.deviceType
            )

            compositeDisposable.add(dataManager.doEditProfileApiCall(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data!!.userUpdate
                            if (data?.status == 200) {
                                setReturnData(data, fieldName, fieldValue)
                            } else if(data?.status == 500) {
                                navigator.openSessionExpire("EPVM")
                            } else {
                                if (data?.errorMessage==null)
                                navigator.showToast(resourceProvider.getString(R.string.Error_msg, fieldName.capitalize()))
                                else navigator.showToast(data?.errorMessage.toString())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, {
                        navigator.showToast(resourceProvider.getString(R.string.Error_msg, fieldName.capitalize()))
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setReturnData(data: EditProfileMutation.UserUpdate, fieldName: String, fieldValue: String) {
        try {
            when (fieldName) {
                "firstName" -> {
                    val jsonArray = JsonParser.parseString(fieldValue).asJsonArray
                    firstName.set(jsonArray.get(0).asString.replace("\"", ""))
                    if(jsonArray.size()>0){
                        lastName.set(jsonArray.get(1).asString.replace("\"", ""))
                    }else{
                        lastName.set(temp1.get())
                    }
                    dataManager.currentUserName = firstName.get() +" "+ lastName.get()
                    dataManager.currentUserFirstName = firstName.get()
                    dataManager.currentUserLastName = lastName.get()
                }
                "phoneNumber" -> { phone.set(temp.get()) }

                "info" -> { aboutMe.set(temp.get()) }
                "gender" -> {
                    when (fieldValue.capitalize()) {
                        "Male" -> {
                            genderLanguage.set(resourceProvider.getString(R.string.radio_male))
                        }
                        "Female" -> {
                            genderLanguage.set(resourceProvider.getString(R.string.radio_female))
                        }
                        "Other" -> {
                            genderLanguage.set(resourceProvider.getString(R.string.radio_other))
                        }
                    }
                    gender.set(fieldValue.capitalize())
                }
                "preferredLanguage" -> {
                    languages.set(temp.get())
                    languagesValue.set(fieldValue)

                }
                "dateOfBirth" -> {
                    dob.set(dob1.get()!![1].toString() + "-" + dob1.get()!![0].toString() + "-" + dob1.get()!![2].toString())
                }
                "email" -> {
                    if (!data.userToken.isNullOrEmpty()) {
                        dataManager.updateAccessToken(data.userToken)
                    }
                    email.set(temp.get())
                }
                "preferredCurrency" -> {
                    currency.set(fieldValue.capitalize())
                    if (fieldValue.capitalize()==BindingAdapters.getCurrencySymbol(fieldValue.capitalize())){
                        currencyWithSymbol.set("${fieldValue.capitalize()}")

                    }else{
                        currencyWithSymbol.set("${BindingAdapters.getCurrencySymbol(fieldValue.capitalize())} ${fieldValue.capitalize()}")

                    }

                    dataManager.currentUserCurrency = fieldValue
                    navigator.openSplashScreen()
                }
                "isEmailConfirmed" -> {

                }
                "isFacebookConnected" -> {

                }
                "isGoogleConnected" -> {

                }
                "isPhoneVerified" -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun setPictureInPref(pic: String?) {
        dataManager.currentUserProfilePicUrl = pic
    }

    fun updateCurrency(it: String) {
        currency.set(it)
        currencySymbol.set(getCurrencySymbol())
        if (it==BindingAdapters.getCurrencySymbol(it)){
            currencyWithSymbol.set("${it}")
        }else{
            currencyWithSymbol.set("${BindingAdapters.getCurrencySymbol(it)} ${it}")

        }
        dataManager.currentUserCurrency = it
    }

    fun getProfileDetails() {
        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data!!.userAccount
                        if (data?.status == 200) {
                            setData(data.result!!)
                            saveData(data.result!!)
                            navigator.showLayout()
                        } else if(data?.status == 500) {
                            navigator.openSessionExpire("EPVM")
                        } else {
                            if (data?.errorMessage==null)
                            navigator.showError()
                            else navigator.showToast(data.errorMessage.toString())
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    fun setData(data: GetProfileQuery.Result) {
        dataManager.currentUserProfilePicUrl = data.picture
        pic.set(data.picture)

        currency.set(dataManager.currentUserCurrency)
        currencySymbol.set(getCurrencySymbol())
        if (dataManager.currentUserCurrency==BindingAdapters.getCurrencySymbol(dataManager.currentUserCurrency)){
                currencyWithSymbol.set("${dataManager.currentUserCurrency} ")
            }else{
                currencyWithSymbol.set("${BindingAdapters.getCurrencySymbol(dataManager.currentUserCurrency)} ${dataManager.currentUserCurrency}")

            }

        if(!data.firstName.isNullOrEmpty() && !data.lastName.isNullOrEmpty()) {
            firstName.set(data.firstName)
            lastName.set(data.lastName)
            dataManager.currentUserName = firstName.get() +" "+ lastName.get()
            dataManager.currentUserFirstName = firstName.get()
            dataManager.currentUserLastName = lastName.get()
        }

        if(!data.info.isNullOrEmpty()) {
            aboutMe.set(data.info)
        }
        if(!data.gender.isNullOrEmpty()) {
            gender.set(data.gender?.capitalize())
            when (data.gender?.capitalize()) {
                "Male" -> {
                    genderLanguage.set(resourceProvider.getString(R.string.radio_male))
                }
                "Female" -> {
                    genderLanguage.set(resourceProvider.getString(R.string.radio_female))
                }
                "Other" -> {
                    genderLanguage.set(resourceProvider.getString(R.string.radio_other))
                }
            }
        }
        if(!data.dateOfBirth.isNullOrEmpty()) {
            dob.set(data.dateOfBirth)
            val string = data.dateOfBirth?.split("-")
            string?.let {
                if (it.isNotEmpty()) {
                    dob1.set(arrayOf(string[1].toInt(), string[0].toInt(), string[2].toInt()))
                }
            }
        } else {
            dob1.set((Utils.get18YearLimit()))
        }
        if(!data.email.isNullOrEmpty()) {
            email.set(data.email)
        }
        if(!data.phoneNumber.isNullOrEmpty()) {
            phone.set(data.countryCode + " " + data.phoneNumber)
            dataManager.currentUserPhoneNo = data.countryCode + " " + data.phoneNumber
            dataManager.currentPhoneNo = data.phoneNumber
            dataManager.countryCode = data.countryCode

        }
        if(!data.preferredLanguageName.isNullOrEmpty()) {
            languages.set(data.preferredLanguageName)
            languagesValue.set(data.preferredLanguage)
        }
        Log.i("Locationedit",data.location.toString())
        if(!data.location.isNullOrEmpty()) {
            location.set(data.location)
        }
        if (!data.createdAt.isNullOrEmpty()) {
            createdAt.set(data.createdAt)
        }

        data.verification?.let {
            dataManager.isEmailVerified = it.isEmailConfirmed
            dataManager.isIdVerified = it.isIdVerification
            dataManager.isGoogleVerified = it.isGoogleConnected
            dataManager.isFBVerified = it.isFacebookConnected
            dataManager.isPhoneVerified = it.isPhoneVerified
            emailVerified.set(dataManager.isEmailVerified)
            fbVerified.set(dataManager.isFBVerified)
            googleVerified.value=dataManager.isGoogleVerified
            phVerified.set(dataManager.isPhoneVerified)
        }
        googleVerified.value=data.verification!!.isGoogleConnected!!
        fbVerify.value=data.verification!!.isFacebookConnected!!
        emailverify.value=data.verification!!.isEmailConfirmed
    }

    fun onTextChanged(text: CharSequence) {
        temp.set(text.toString())
        navigator.hideSnackbar()
    }

    fun onTextChanged1(text: CharSequence) {
        temp1.set(text.toString())

        navigator.hideSnackbar()
    }

    fun clickEvent(layoutID: Int) {
        layoutId.set(layoutID)
        navigator.openEditScreen()
    }
    val code = ObservableField("")
    val profileDetails = MutableLiveData<ProfileDetails?>()
    val loadedApis = MutableLiveData<ArrayList<Int>?>()

    val verificationType = MutableLiveData<String>()

    init {
        loadedApis.value = arrayListOf()
    }



    private fun saveData(data: GetProfileQuery.Result) {
        try {
            profileDetails.value = ProfileDetails(
                userName = data.firstName + " " + data.lastName,
                createdAt = data.createdAt,
                picture = data.picture,
                email = data.email,
                emailVerification = data.verification?.isEmailConfirmed,
                idVerification = data.verification?.isIdVerification,
                googleVerification= data.verification?.isGoogleConnected,
                fbVerification = data.verification?.isFacebookConnected,
                phoneVerification = data.verification?.isPhoneVerified,
                userType = data.loginUserType,
                addedList = data.isAddedList
            )

            // Save data in pref
            dataManager.currentUserProfilePicUrl = data.picture
            dataManager.currentUserFirstName = data.firstName
            dataManager.currentUserLastName = data.lastName
            dataManager.isEmailVerified = data.verification?.isEmailConfirmed
            dataManager.isIdVerified = data.verification?.isIdVerification
            dataManager.isGoogleVerified = data.verification?.isGoogleConnected
            dataManager.isFBVerified = data.verification?.isFacebookConnected
            dataManager.isPhoneVerified = data.verification?.isPhoneVerified
            dataManager.currentUserCreatedAt = data.createdAt
            dataManager.currentUserEmail = data.email
            dataManager.currentUserType = data.loginUserType
            dataManager.isListAdded = data.isAddedList

        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun sendVerifyEmail() {
        val buildQuery = SendConfirmEmailQuery()
        compositeDisposable.add(dataManager.sendConfirmationEmail(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe( { response ->
                try {
                    removeApiTolist(1)
                    val data = response.data!!.resendConfirmEmail
                    if (data!!.status == 200) {
                        navigator.showToast(resourceProvider.getString(R.string.confirmation_link_is_sent_to_your_email))
                    } else if(data.status == 500) {
                        navigator.openSessionExpire("TrustVM")
                    } else {
                        data.errorMessage?.let {
                            navigator.showToast(it)
                        } ?: navigator.showToast(resourceProvider.getString(R.string.invalid_link))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                } }, {
                addApiTolist(1)
                it.printStackTrace()
                handleException(it)
            } )
        )
    }

    fun sendConfirmCode() {
        try {
            val mutate = CodeVerificationMutation(
                email = email.get()!!.toString(),
                token = code.get()!!.toString()
            )

            compositeDisposable.add(dataManager.ConfirmCodeApiCall(mutate)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .subscribe( { response ->
                    try {
                        removeApiTolist(2)
                        val data = response.data!!.emailVerification
                        if (data!!.status == 200) {
                            editProfileDetails("email", true)
                            getProfileDetails()

                            emailverify.value=true
                            navigator.showToast(resourceProvider.getString(R.string.your_email_is_confirmed))
                        } else if(data.status == 500) {
                            navigator.openSessionExpire("TrustVM")
                        } else {
                            data.errorMessage?.let {
                                navigator.showToast(it)
                            } ?: navigator.showToast(resourceProvider.getString(R.string.invalid_link))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    addApiTolist(2)
                    handleException(it)
                } )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun socialLoginVerify(actionType : String , type : String) {
        val buildQuery = SocialLoginVerifyMutation(
            actionType = actionType,
            verificationType = type
        )

        compositeDisposable.add(dataManager.SocialLoginVerify(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .subscribe({ response ->
                try {
                    removeApiTolist(3)
                    val data = response.data!!.socialVerification
                    if (data!!.status == 200) {
                        if(type == "facebook") {
                            if(actionType == "true") {
                                editProfileDetails("facebook", true)
                                dataManager.isFBVerified = true
                                verificationType.value = "facebook"
                                fbVerify.value=true
                                navigator.showToast(resourceProvider.getString(R.string.facebook_connected))
                            }
                            else {
                                editProfileDetails("facebook", false)
                                dataManager.isFBVerified = false
                                fbVerify.value=false
                                verificationType.value = "facebook"
                                navigator.showToast(resourceProvider.getString(R.string.facebook_disconnected))
                            }
                        } else {
                            if(actionType == "true") {
                                editProfileDetails("google", true)
                                dataManager.isGoogleVerified = true
                                googleVerified.value=true
                                verificationType.value = "google"
                                navigator.showToast(resourceProvider.getString(R.string.google_connected))
                            }
                            else {
                                editProfileDetails("google", false)
                                dataManager.isGoogleVerified = false
                                googleVerified.value=false

                                verificationType.value = "google"
                                navigator.showToast(resourceProvider.getString(R.string.google_disconnected))
                            }
                        }
                    } else if (data.status == 500){
                        navigator.openSessionExpire("TrustVM")
                    } else{
                        navigator.showError()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                addApiTolist(3)
                handleException(it, true)
            }))
    }

    fun editProfileDetails(type: String, flag: Boolean) {
        val data = profileDetails.value
        data?.let {
            if (type == "facebook") {
                it.fbVerification = flag
            } else if (type == "google") {
                it.googleVerification = flag
            } else {
                it.emailVerification = flag
            }
        }
        profileDetails.value = data
    }

    fun addApiTolist(id: Int) {
        val api = loadedApis.value
        api?.add(id)
        loadedApis.value = api
    }

    fun removeApiTolist(id: Int) {
        val api = loadedApis.value
        api?.remove(id)
        loadedApis.value = api
    }
}