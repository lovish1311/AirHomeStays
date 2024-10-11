package com.airhomestays.app.ui.trips.contactus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airhomestays.app.R
import com.airhomestays.app.databinding.DialogContactBinding
import com.airhomestays.app.ui.base.BaseDialogFragment
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ContactSupport : BaseDialogFragment(), ContactUsNavigator {
    override fun closeDialog() {
        dismissDialog()
    }

    private val TAG = ContactSupport::class.java.simpleName
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: ContactUsViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ContactUsViewModel::class.java)

    companion object {
        private const val LISTID = "param1"
        private const val RESERVATIONID = "param2"
        @JvmStatic
        fun newInstance(listId: Int, reservationId: Int) =
                ContactSupport().apply {
                    arguments = Bundle().apply {
                        putInt(LISTID, listId)
                        putInt(RESERVATIONID, reservationId)
                    }
                }
    }

    fun dismissDialog() {
        dismissDialog(TAG)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<DialogContactBinding>(inflater, R.layout.dialog_contact, container, false)
        val view = binding.root
        AndroidSupportInjection.inject(this)
        viewModel.navigator = this
        viewModel.setInitialValuesFromIntent(arguments)
        binding.title = resources.getString(R.string.contact_us)
        binding.msg = viewModel.msg
        binding.isLoading = viewModel.isLoading
        binding.ltLoading.gone()
        binding.btnCancel.onClick { dismissDialog() }
        binding.btnApply.onClick {
            hideKeyboard()
            if (viewModel.listId.value != null && viewModel.reservationId.value != null) {
                viewModel.validateMsg()
                if(viewModel.msg.get()!!.isNotEmpty()) {
                    viewModel.validateMsg()
                } else {
                    showToast(resources.getString(R.string.please_enter_the_message))
                }
            } else {
                showToast(resources.getString(R.string.something_went_wrong))
            }
        }
        return view
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }
}