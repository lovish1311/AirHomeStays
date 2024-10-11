package com.airhomestays.app.ui.saved.createlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentCreatelistBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderListingDetailsSectionHeader
import com.airhomestays.app.viewholderSavedEt
import com.airhomestays.app.viewholderSavedPlaceholder
import javax.inject.Inject

class CreateListActivity : BaseActivity<FragmentCreatelistBinding, CreateListViewModel>(),
    CreatelistNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCreatelistBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_createlist
    override val viewModel: CreateListViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(CreateListViewModel::class.java)
    private var mCurrentState = State.IDLE
    private var selectArray = arrayOf(true, false)
    var edit = 0
    var nameTitle = ""

    enum class State {
        EXPANDED,
        IDLE
    }

    companion object {
        @JvmStatic
        fun openCreateListActivity(context: Context, listId: Int) {
            val intent = Intent(context, CreateListActivity::class.java)
            intent.putExtra("listID", listId)
            intent.putExtra("edit", 1)
            intent.putExtra("name", "")
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        val listid = intent.getIntExtra("listID", 0)
        edit = intent.getIntExtra("edit", 0)
        nameTitle = intent.getStringExtra("name")!!
        viewModel.setvalue(listid, edit)

        if (!nameTitle.isNullOrEmpty()) {
            viewModel.title.set(nameTitle)
        }
        mBinding.ivClose.onClick { finish() }
        EpoxyVisibilityTracker().attach(mBinding.rvCreateList)
        setUp()
    }

    private fun setUp() {
        try {
            if (edit == 1) {
                mBinding.tvCreate.text = getString(R.string.save)
            }
            mBinding.rvCreateList.withModels {
                viewholderListingDetailsSectionHeader {
                    id("header")
                    if (edit == 1) {
                        header(resources.getString(R.string.edit_a_list))
                    } else {
                        header(resources.getString(R.string.create_a_list))
                    }
                    onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                        mCurrentState = if (percentVisibleHeight < 99) {
                            if (mCurrentState != State.EXPANDED) {
                                ViewCompat.setElevation(mBinding.ablSaved, 5F)
                            }
                            State.EXPANDED
                        } else {
                            if (mCurrentState != State.IDLE) {
                                ViewCompat.setElevation(mBinding.ablSaved, 0F)
                            }
                            State.IDLE
                        }
                    }
                }
                viewholderSavedPlaceholder {
                    id("title")
                    header(resources.getString(R.string.title))
                    isBlack(true)
                }
                viewholderSavedEt {
                    id("titleEditText")
                    msg(viewModel.title)
                }
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    override fun onRetry() {
        viewModel.validateData()
    }


    override fun navigate(isLoadSaved: Boolean) {
        if (isLoadSaved) {
            val intent = Intent()
            setResult(2, intent)
            finish()
        } else {
            finish()
        }
    }
}