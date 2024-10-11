package com.airhomestays.app.ui.host.step_two

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostActivityStepTwoBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.util.addFragmentToActivity
import com.airhomestays.app.util.replaceFragmentInActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class StepTwoActivity : BaseActivity<HostActivityStepTwoBinding,StepTwoViewModel>(),StepTwoNavigator{


    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostActivityStepTwoBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_step_two
    override val viewModel: StepTwoViewModel
        get() = ViewModelProvider(this,mViewModelFactory).get(StepTwoViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        viewModel.listID = intent.getStringExtra("listID").orEmpty()
        subscribeToLiveData(savedInstanceState)
    }

    fun subscribeToLiveData(savedInstanceState: Bundle?){
        viewModel.listSetting().observe(this, Observer {
            it.let {
                if (savedInstanceState == null) {
                        initView()
                    }
                }
        })
    }

    private fun initView() {
        addFragment(UploadListingPhotos(), "step1")
    }

    private fun addFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        addFragmentToActivity(mBinding.flStepTwo.id, fragment, tag)
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flStepTwo.id, fragment, tag)
    }

    override fun navigateToScreen(screen: StepTwoViewModel.NextScreen, vararg params: String?) {
        when(screen){
            StepTwoViewModel.NextScreen.COVER -> {
                replaceFragment(CoverPhotoFragment(),"CoverPhoto")
            }
            StepTwoViewModel.NextScreen.LISTTITLE -> {
                replaceFragment(AddListTitleFragment(), "AddListPhoto")
            }
            StepTwoViewModel.NextScreen.LISTDESC -> {
                if(viewModel.title.get().isNullOrEmpty()) {
                    showSnackbar( "Please add a title to your list.", "Add title")

                }else{
                    hideKeyboard()
                    viewModel.listDetailsStep2.value!!.title = viewModel.title.get()
                    replaceFragment(AddListDescFragment(), "ListDesc")
                }
            }
            StepTwoViewModel.NextScreen.APIUPDATE -> {
                hideKeyboard()
                    if(viewModel.desc.get().isNullOrEmpty()){
                        showSnackbar( "Please add a description to your list.", "Add description")
                    }else{
                        viewModel.retryCalled = "update"
                        viewModel.listDetailsStep2.value!!.desc = viewModel.desc.get()
                        viewModel.updateStep2()
                    }
            }
            StepTwoViewModel.NextScreen.FINISH -> {
                this.finish()
            }

            StepTwoViewModel.NextScreen.UPLOAD -> {
                //uploadImage(params[0])
            }

        }
    }

    fun uploadImage(){

    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onRetry() {
        if (isNetworkConnected) {
            if (viewModel.retryCalled.equals("")) {
                viewModel.step2Retry()
            } else if (viewModel.retryCalled.contains("delete")) {
                viewModel.showListPhotos(viewModel.retryCalled)
            } else {
                viewModel.updateStep2()
            }
        }

    }

    override fun onBackPressed() {
        val fm = supportFragmentManager.findFragmentByTag("AddListPhoto")
        if(fm!=null) {
            if (fm.isVisible) {
                if (viewModel.title.get().equals("")) {
                    showSnackbar("Please add a title to your list.", "Add title")
                } else {
                    super.onBackPressed()
                }
            } else {
                val fm = supportFragmentManager.findFragmentByTag("ListDesc")
                if (fm != null) {
                    if (fm.isVisible) {
                        if (viewModel.desc.get().equals("")) {
                            showSnackbar("Please add a description to your list.", "Add description")
                        } else {
                            super.onBackPressed()
                        }
                    }
                } else {
                    super.onBackPressed()
                }
            }
        }else{
            super.onBackPressed()
        }
    }


}