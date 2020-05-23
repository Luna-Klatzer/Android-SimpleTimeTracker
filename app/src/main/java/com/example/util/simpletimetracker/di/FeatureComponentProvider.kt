package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.di.ChartFilterComponentProvider
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider

interface FeatureComponentProvider :
    RunningRecordsComponentProvider,
    ChangeRecordTypeComponentProvider,
    RecordsComponentProvider,
    ChangeRecordComponentProvider,
    StatisticsComponentProvider,
    SettingsComponentProvider,
    ChartFilterComponentProvider {

    var appComponent: AppComponent?
}