package com.airhomestays.app.ui.auth.name

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentAuthNameCreationBinding
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.dobcalendar.BirthdayDialog
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.UiEvent
import com.airhomestays.app.util.Utils.Companion.clickWithDebounce
import com.airhomestays.app.util.onClick
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NameCreationFragment : BaseFragment<FragmentAuthNameCreationBinding, NameCreationViewModel>(),
    AuthNavigator {

    private lateinit var mBinding: FragmentAuthNameCreationBinding
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_name_creation
    override val viewModel: NameCreationViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(NameCreationViewModel::class.java)
    private var param1: String = ""
    private var param2: String = ""

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NameCreationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1, "")
            param2 = it.getString(ARG_PARAM2, "")
        }
        viewModel.firstName.set(param1)
        viewModel.lastName.set(param2)

        viewModel.isbirthdaySelected = false;
    }

    override fun onRetry() {
        viewModel.signupUser()
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    private fun initView() {
        mBinding.ivPasswordVisibility.setOnClickListener { showPassword() }
        mBinding.ivClose.setOnClickListener { navigateScreen(AuthViewModel.Screen.AuthScreen) }
        mBinding.rlBirthday.onClick {
            hideSnackbar()
            openCalender()
        }
        mBinding.btnSignup.clickWithDebounce(300) {
            hideKeyboard()

            if (viewModel.firstName.get()!!.trim().isNullOrEmpty() && viewModel.lastName.get()!!
                    .trim().isNullOrEmpty()
            ) {
                showSnackbar(
                    getString(R.string.invalid_name),
                    getString(R.string.invalid_name_desc)
                )
            } else if (viewModel.firstName.get()!!.trim().isNullOrEmpty()) {
                showSnackbar(
                    getString(R.string.invalid_name),
                    getString(R.string.invalid_firstname_desc)
                )
            } else if (viewModel.lastName.get()!!.trim().isNullOrEmpty()) {
                showSnackbar(
                    getString(R.string.invalid_name),
                    getString(R.string.invalid_lastname_desc)
                )
            } else {
                if (!viewModel.isLoading.get()) {
                    viewModel.lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
                    viewModel.checkEmail()
                }
            }

        }

    }

    private fun openCalender() {
        val birthdayDialog = BirthdayDialog.newInstance(
            viewModel.dob.get()!![0],
            viewModel.dob.get()!![1],
            viewModel.dob.get()!![2]
        )
        birthdayDialog.show(childFragmentManager)

        birthdayDialog.setCallBack(DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            viewModel.yearLimit.set(arrayOf(year, month + 1, dayOfMonth))
            viewModel.dob.set(arrayOf(year, month, dayOfMonth))
            viewModel.dobString.set(arrayOf(year, month, dayOfMonth).toString())
            mBinding.tvMonth.setTextColor(getResources().getColor(R.color.black))
            mBinding.tvMonth.alpha = 1F
            mBinding.tvYear.setTextColor(getResources().getColor(R.color.black))
            mBinding.tvYear.alpha = 1F
            mBinding.tvDay.setTextColor(getResources().getColor(R.color.black))
            mBinding.tvDay.alpha = 1F
            mBinding.firstDivider.alpha = 1F
            mBinding.secondDivider.alpha = 1F
            viewModel.isbirthdaySelected = true
        })


    }

    fun showPassword() {
        if (viewModel.showPassword.get() == false) {
            viewModel.showPassword.set(true)
            mBinding.ivPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_visibility_show
                )
            )
        } else {
            viewModel.showPassword.set(false)
            mBinding.ivPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_visibility_hide,
                )
            )
        }
    }
}