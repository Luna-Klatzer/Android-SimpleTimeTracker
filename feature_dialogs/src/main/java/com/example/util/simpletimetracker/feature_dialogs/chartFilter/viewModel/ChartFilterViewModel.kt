package com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.mapper.ChartFilterViewDataMapper
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData.ChartFilterRecordTypeViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChartFilterViewModel @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        updateRecordTypes()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var types: List<RecordType> = emptyList()
    private var typeIdsFiltered: MutableList<Long> = mutableListOf()

    fun onRecordTypeClick(item: ChartFilterRecordTypeViewData) {
        viewModelScope.launch {
            if (item.id in typeIdsFiltered) {
                typeIdsFiltered.remove(item.id)
            } else {
                typeIdsFiltered.add(item.id)
            }
            prefsInteractor.setFilteredTypes(typeIdsFiltered)
            updateRecordTypes()
        }
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        if (types.isEmpty()) types = loadRecordTypes()
        (recordTypes as MutableLiveData).value = types
            .map { type -> chartFilterViewDataMapper.map(type, typeIdsFiltered) }
            .apply {
                this as MutableList
                add(chartFilterViewDataMapper.mapToUntrackedItem(typeIdsFiltered))
            }
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        val typesInStatistics = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()
        typeIdsFiltered = prefsInteractor.getFilteredTypes()
            .toMutableList()

        return recordTypeInteractor.getAll()
            .filter { it.id in typesInStatistics }
    }
}
