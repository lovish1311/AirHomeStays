package com.airhomestays.app.ui.profile.edit_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityEditProfileBinding
import com.airhomestays.app.databinding.FragmentEditProfileLocationBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class EditProfileLocation : BaseFragment<FragmentEditProfileLocationBinding, EditProfileViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var activityEditProfileBinding: FragmentEditProfileLocationBinding
    override val bindingVariable: Int
    get() = BR.viewModel
    override val layoutId: Int
    get() = R.layout.fragment_edit_profile_location
    override val viewModel: EditProfileViewModel
    get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(EditProfileViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityEditProfileBinding = viewDataBinding!!
        activityEditProfileBinding.actionBar.tvToolbarHeading.text=""

        activityEditProfileBinding.actionBar.ivNavigateup.onClick {
            viewModel.navigator.moveToBackScreen()
        }
        activityEditProfileBinding.btnAdd.onClick {
            //viewModel.checkLocation()
        }
    }

    override fun onRetry() {}
}