package com.airhomestays.app.ui.profile.manageAccount

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ManageAccountActivityBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class ManageAccountActivity : BaseActivity<ManageAccountActivityBinding, ManageAccountViewModel>(),
    ManageAccountNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var mBinding: ManageAccountActivityBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.manage_account_activity
    override val viewModel: ManageAccountViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ManageAccountViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this

        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.manage_account)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }
        setUp()
        mBinding.deleteActTxt.append(
            getColoredString(
                resources.getString(R.string.delete_account_desc),
                resources.getColor(R.color.grey_font)
            )
        );

    }

    fun getColoredString(text: CharSequence?, color: Int): Spannable {
        val spannable: Spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.are_you_sure_delete_account_permenently))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.getDeleteAccount(applicationContext)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun setUp() {
        mBinding.permenentLl.onClick {
            showDeleteConfirm()
        }
    }


    override fun onRetry() {

    }

    override fun navigateScreen(
        OpenScreen: ManageAccountViewModel.OpenScreen,
        vararg params: String?
    ) {
        when (OpenScreen) {
            ManageAccountViewModel.OpenScreen.LOGIN -> {
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }

            ManageAccountViewModel.OpenScreen.FINISHED -> {

            }
        }
    }

    override fun closeDialog() {

    }
}