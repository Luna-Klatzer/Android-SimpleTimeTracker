package com.example.util.simpletimetracker.feature_settings.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_settings_views.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings_views.SettingsBottomViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTextViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTopViewData
import com.example.util.simpletimetracker.feature_settings_views.SettingsTranslatorViewData
import javax.inject.Inject

class SettingsContributorsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun execute(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        result += SettingsTopViewData(
            block = SettingsBlock.ContributorsTop,
        )

        result += SettingsTextViewData(
            block = SettingsBlock.ContributorsTitle,
            title = resourceRepo.getString(R.string.settings_contributors),
            subtitle = "",
            layoutIsClickable = false,
        )

        result += loadContributorsViewData()

        result += SettingsBottomViewData(
            block = SettingsBlock.ContributorsBottom,
        )

        return result
    }

    private fun loadContributorsViewData(): List<SettingsTranslatorViewData> {
        return resourceRepo
            .getStringArray(R.array.contributors)
            .map { SettingsTranslatorViewData(it) }
    }
}