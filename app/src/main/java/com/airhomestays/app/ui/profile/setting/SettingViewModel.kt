package com.airhomestays.app.ui.profile.setting

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.*
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import java.util.*
import javax.inject.Inject


class SettingViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<SettingsNavigator>(dataManager, resourceProvider) {

    var baseCurrency = ObservableField("")

    var appLanguage = ObservableField("English")
    var appTheme = MutableLiveData("Auto")
    val oldPassword = ObservableField("")
    val showOldPassword = ObservableField(false)
    val newPassword = ObservableField("")
    val showNewPassword = ObservableField(false)
    val confirmPassword = ObservableField("")
    val showConfirmPassword = ObservableField(false)
    val isEmailUser = MutableLiveData(false)

    lateinit var currencies: MutableLiveData<List<GetCurrenciesListQuery.Result?>?>
    lateinit var language: MutableLiveData<List<UserPreferredLanguagesQuery.Result>>

    val langName =
        arrayOf("English", "Español", "Français", "Italiano", "Português", "العربية", "ישראל")
    val langCode = arrayOf("en", "es", "fr", "it", "pt", "ar", "iw")
    val theme = arrayOf("Auto", "Light", "Dark")
    val themeIcon = arrayOf(R.drawable.ic_auto, R.drawable.ic_light_theme, R.drawable.ic_dark)

    fun loadSettingData(): MutableLiveData<List<GetCurrenciesListQuery.Result?>?> {
        if (!::currencies.isInitialized) {
            currencies = MutableLiveData()
            baseCurrency.set(dataManager.currentUserCurrency)
            getProfileDetails()
            getCurrency()

        }
        return currencies
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery()
        compositeDisposable.add(dataManager.getCurrencyList(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data!!.getCurrencies
                if (data?.status == 200) {
                    setLang()
                    currencies.value = data.results
                } else if (data?.status == 500) {
                    if (data.errorMessage == null)
                        navigator.showError()
                    else navigator.showToast(data.errorMessage.toString())
                }
            }, {
                handleException(it)
            })
        )
    }

    fun setLang() {
        langCode.forEachIndexed { index, s ->
            if (s.equals(Locale.getDefault().toString())) {
                appLanguage.set(langName[index])
            }
        }
    }


    fun signOut() {
        val buildQuery = LogoutMutation(
            deviceType = Constants.deviceType,
            deviceId = dataManager.firebaseToken.toString()
            )
        compositeDisposable.add(dataManager.doLogoutApiCall(buildQuery)

            .doFinally {
                afterSignOut()
            }
            .performOnBackOutOnMain(scheduler)
            .subscribe({
                afterSignOut()
                navigator.navigateToSplash()
            }, {
                afterSignOut()
                navigator.navigateToSplash()
            })
        )
    }

    fun updateProfile(fieldName: String, fieldValue: String, context: Context) {
        try {
            val mutate = EditProfileMutation(
                fieldName = fieldName,
                fieldValue = fieldValue.toOptional(),
                deviceId = dataManager.firebaseToken!!,
                userId = dataManager.currentUserId!!,
                deviceType = Constants.deviceType
                )

            compositeDisposable.add(
                dataManager.doEditProfileApiCall(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data!!.userUpdate
                            if (data?.status == 200) {
                                setReturnData(fieldValue, fieldName, context)
                            } else if (data?.status == 500) {
                                navigator.openSessionExpire("SettingVM")
                            } else {
                                if (data?.errorMessage == null)
                                    navigator.showToast(
                                        resourceProvider.getString(
                                            R.string.Error_msg,
                                            fieldName.capitalize()
                                        )
                                    )
                                else navigator.showToast(data.errorMessage.toString())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        navigator.showToast(
                            resourceProvider.getString(
                                R.string.Error_msg,
                                fieldName.capitalize()
                            )
                        )
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }

    }

    private fun setReturnData(fieldValue: String, fieldName: String, context: Context) {
        try {
            val pref: SharedPreferences =
                context.getSharedPreferences("THEME", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()

            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager




            if (fieldName == "appTheme") {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    uiModeManager.setApplicationNightMode(
//                        when (item.isChecked) {
//                            true -> UiModeManager.MODE_NIGHT_YES
//                            else -> UiModeManager.MODE_NIGHT_NO
//                        }
//                    )}

                    when (fieldValue) {
                    "Auto" -> {
                        appTheme.value = "Auto"
                        dataManager.prefTheme = "Auto"
                        editor.putString("appTheme", "Auto")
                        editor.commit()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
                        }else{
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }


                    }

                    "Dark" -> {
                        appTheme.value = "Dark"
                        dataManager.prefTheme = "Dark"
                        editor.putString("appTheme", "Dark")
                        editor.commit()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                        }else{
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }
                    }

                    else -> {
                        appTheme.value = "Light"
                        dataManager.prefTheme = "Light"
                        editor.putString("appTheme", "Light")
                        editor.commit()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                        }else{
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun updateCurrency(context: Context,it: String) {
        updateProfile("preferredCurrency",it,context)
        baseCurrency.set(it)
        dataManager.currentUserCurrency = it
        navigator.openSplashScreen()

    }

    fun updateLangauge(context: Context,it: String, label: String) {
        updateProfile("preferredLanguage",it,context)
        appLanguage.set(label)
        dataManager.currentUserLanguage = it
        navigator.setLocale(it)
    }

    fun updateTheme(context: Context, appTheme: String) {


        when (appTheme) {
            "Auto" -> {
                updateProfile("appTheme", "Auto", context)
            }

            "Dark" -> {
                updateProfile("appTheme", "Dark", context)
            }

            else -> {
                updateProfile("appTheme", "Light", context)
            }
        }
    }

    fun getProfileDetails() {

        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.userAccount
                    if (data!!.status == 500) {
                        if (dataManager.isUserLoggedIn()) navigator.openSessionExpire("")
                    }
                    isEmailUser.value = data.result?.userData?.type == "email"

                    if (!data.result?.appTheme.isNullOrEmpty()) {
                        if (data.result?.appTheme == "auto") {
                            dataManager.prefTheme = "Auto"
                            appTheme.value = dataManager.prefTheme.toString()
                        } else if (data.result?.appTheme == "dark") {
                            dataManager.prefTheme = "Dark"
                            appTheme.value = dataManager.prefTheme.toString()
                        } else {
                            dataManager.prefTheme = "Light"
                            appTheme.value = dataManager.prefTheme.toString()
                        }
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }

    private fun afterSignOut() {
        dataManager.setUserAsLoggedOut()
    }

    fun checkPassword() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        if (oldPassword.get()!!.isBlank()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.password_error),
                resourceProvider.getString(R.string.old_password_err)
            )
        } else if (newPassword.get()!!.length < 8 || newPassword.get()!!.isBlank()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.password_error),
                resourceProvider.getString(R.string.new_password_err)
            )
        } else if (confirmPassword.get()!!.length < 8 || confirmPassword.get()!!.isBlank()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.password_error),
                resourceProvider.getString(R.string.confirm_password_err)
            )
        } else if (confirmPassword.get().toString() != newPassword.get().toString()) {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.password_error),
                resourceProvider.getString(R.string.new_password_mismatch_err)
            )
        } else {
                changePassword()
        }
    }

    private fun changePassword() {
        val buildQuery = ChangePasswordMutation(
            oldPassword = oldPassword.get().toOptional(),
            newPassword = newPassword.get().toOptional(),
            confirmPassword = confirmPassword.get().toOptional()
        )
        compositeDisposable.add(dataManager.doChangePasswordApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.changePassword
                    if (data?.status == 200) {
                        navigator.showToast(resourceProvider.getString(R.string.password_updated))
                        navigator.finishActivity()
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        navigator.showSnackbar(resourceProvider.getString(R.string.password_error),data?.errorMessage.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }
}