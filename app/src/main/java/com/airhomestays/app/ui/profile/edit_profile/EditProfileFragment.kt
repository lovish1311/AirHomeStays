package com.airhomestays.app.ui.profile.edit_profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityEditProfileBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class EditProfileFragment: BaseFragment<ActivityEditProfileBinding, EditProfileViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var activityEditProfileBinding: ActivityEditProfileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_edit_profile
    override val viewModel: EditProfileViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(EditProfileViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityEditProfileBinding = viewDataBinding!!
        activityEditProfileBinding.btnAdd.onClick {
            viewModel.done()
        }
        activityEditProfileBinding.btnCancel.onClick {
            viewModel.navigator.moveToBackScreen()
        }
    }

    override fun onRetry() { }
}