package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.extension.orZero
import java.util.Calendar
import com.example.util.simpletimetracker.feature_dialogs.databinding.DateDialogFragmentBinding as Binding

class DateDialogFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    interface OnDateSetListener {
        fun onDateSet(year: Int, monthOfYear: Int, dayOfMonth: Int)
    }

    var listener: OnDateSetListener? = null

    private val timestamp: Long by lazy {
        arguments?.getLong(ARGS_TIMESTAMP, 0).orZero()
    }
    private val firstDayOfWeek: Int by lazy {
        arguments?.getInt(ARGS_FIRST_DAY_OF_WEEK, Calendar.MONDAY)
            ?: Calendar.MONDAY
    }

    override fun initUi(): Unit = with(binding) {
        val calendar = Calendar.getInstance()
            .apply { timeInMillis = timestamp }

        if (BuildVersions.isLollipopOrHigher()) {
            datePicker.firstDayOfWeek = firstDayOfWeek
        }
        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ) { _, year, monthOfYear, dayOfMonth ->
            listener?.onDateSet(year, monthOfYear, dayOfMonth)
        }
    }

    fun getSelectedDate(): Triple<Int, Int, Int> = with(binding) {
        return Triple(datePicker.year, datePicker.month, datePicker.dayOfMonth)
    }

    companion object {
        private const val ARGS_TIMESTAMP = "args_timestamp"
        private const val ARGS_FIRST_DAY_OF_WEEK = "args_first_day_of_week"

        fun newInstance(
            timestamp: Long,
            firstDayOfWeek: Int,
        ) = DateDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARGS_TIMESTAMP, timestamp)
                putInt(ARGS_FIRST_DAY_OF_WEEK, firstDayOfWeek)
            }
        }
    }
}