package com.example.util.simpletimetracker.feature_records_all

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.viewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records_all.di.RecordsAllComponentProvider
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.SnackBarParams
import kotlinx.android.synthetic.main.records_all_fragment.rvRecordsAllList
import javax.inject.Inject

class RecordsAllFragment : BaseFragment(R.layout.records_all_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RecordsAllViewModel>

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsAllViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val recordsAdapter: RecordAllAdapter by lazy {
        RecordAllAdapter(viewModel::onRecordClick)
    }

    override fun initDi() {
        (activity?.application as RecordsAllComponentProvider)
            .recordsAllComponent
            ?.inject(this)
    }

    override fun initUi() {
        parentFragment?.postponeEnterTransition()

        rvRecordsAllList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter

            viewTreeObserver.addOnPreDrawListener {
                parentFragment?.startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = RecordsAllExtra(
                typeId = arguments?.getLong(ARGS_TYPE_ID).orZero()
            )
            records.observe(viewLifecycleOwner, recordsAdapter::replace)
        }
        with(removeRecordViewModel) {
            needUpdate.observe(viewLifecycleOwner) {
                if (it && this@RecordsAllFragment.isResumed) {
                    viewModel.onNeedUpdate()
                    removeRecordViewModel.onUpdated()
                }
            }
            with(removeRecordViewModel) {
                message.observe(viewLifecycleOwner, ::showMessage)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && this.isResumed) {
            router.show(Notification.SNACK_BAR, message)
            removeRecordViewModel.onMessageShown()
        }
    }

    companion object {
        private const val ARGS_TYPE_ID = "args_type_id"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is RecordsAllParams -> putLong(ARGS_TYPE_ID, data.typeId)
            }
        }
    }
}
