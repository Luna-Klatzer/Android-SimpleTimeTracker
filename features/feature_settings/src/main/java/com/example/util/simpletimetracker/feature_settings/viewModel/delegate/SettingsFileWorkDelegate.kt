package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.core.text.HtmlCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.FileWorkRepo
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.interactor.BackupInteractor
import com.example.util.simpletimetracker.domain.interactor.CsvExportInteractor
import com.example.util.simpletimetracker.domain.interactor.IcsExportInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.navigation.RequestCode
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.ActionParams
import com.example.util.simpletimetracker.navigation.params.action.CreateFileParams
import com.example.util.simpletimetracker.navigation.params.action.OpenFileParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.PartialRestoreParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsFileWorkDelegate @Inject constructor(
    private val fileWorkRepo: FileWorkRepo,
    private val resourceRepo: ResourceRepo,
    private val permissionRepo: PermissionRepo,
    private val router: Router,
    private val backupInteractor: BackupInteractor,
    private val csvExportInteractor: CsvExportInteractor,
    private val icsExportInteractor: IcsExportInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticBackupInteractor: AutomaticBackupInteractor,
    private val automaticExportInteractor: AutomaticExportInteractor,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
) : ViewModelDelegate() {

    var partialBackupRestoreData: PartialBackupRestoreData? = null
    var partialBackupRestoreDataSelectable: PartialBackupRestoreData? = null
    private var dataExportSettingsResult: DataExportSettingsResult? = null
    private var saveOptionsData: BackupOptionsData.Save? = null
    private var restoreOptionsData: BackupOptionsData.Restore? = null

    fun onAppVisible() {
        delegateScope.launch {
            checkForAutomaticBackupError()
        }
    }

    fun onRestoreConfirmed() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_OPEN_FILE,
            work = ::onRestoreBackup,
            params = OpenFileParams(
                type = FILE_TYPE_BIN_OPEN,
                notHandledCallback = ::onFileOpenError,
            ),
        )
    }

    fun onPartialRestoreClick() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_OPEN_FILE,
            work = ::onPartialRestore,
            params = OpenFileParams(
                type = FILE_TYPE_BIN_OPEN,
                notHandledCallback = ::onFileOpenError,
            ),
        )
    }

    fun onPartialRestoreConfirmed(
        data: PartialBackupRestoreData,
    ) {
        onPartialRestoreBackup(data)
    }

    fun onCsvImportConfirmed() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_OPEN_FILE,
            work = ::onImportCsvFile,
            params = OpenFileParams(
                type = FILE_TYPE_CSV_OPEN,
                notHandledCallback = ::onFileOpenError,
            ),
        )
    }

    fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        dataExportSettingsResult = data
    }

    fun onCsvExport() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
            work = ::onSaveCsvFile,
            params = CreateFileParams(
                fileName = "stt_records_${getFileNameTimeStamp()}.csv",
                type = FILE_TYPE_CSV,
                notHandledCallback = ::onFileCreateError,
            ),
        )
    }

    fun onIcsExport() {
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
            work = ::onSaveIcsFile,
            params = CreateFileParams(
                fileName = "stt_events_${getFileNameTimeStamp()}.ics",
                type = FILE_TYPE_ICS,
                notHandledCallback = ::onFileCreateError,
            ),
        )
    }

    fun onFileWork() {
        delegateScope.launch {
            checkForAutomaticBackupError()
            requestScreenUpdate()
        }
    }

    fun onSaveClick(params: BackupOptionsData.Save) {
        saveOptionsData = params
        requestFileWork(
            requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
            work = ::onSaveBackup,
            params = CreateFileParams(
                fileName = "stt_${getFileNameTimeStamp()}.backup",
                type = FILE_TYPE_BIN,
                notHandledCallback = ::onFileCreateError,
            ),
        )
    }

    fun onAutomaticBackupClick() = delegateScope.launch {
        if (loadAutomaticBackupEnabled()) {
            disableAutomaticBackup()
        } else {
            requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                work = ::onAutomaticBackup,
                params = CreateFileParams(
                    fileName = "stt_automatic.backup",
                    type = FILE_TYPE_BIN,
                    notHandledCallback = ::onFileCreateError,
                ),
            )
        }
    }

    fun onAutomaticExportClick() = delegateScope.launch {
        if (loadAutomaticExportEnabled()) {
            disableAutomaticExport()
        } else {
            requestFileWork(
                requestCode = RequestCode.REQUEST_CODE_CREATE_FILE,
                work = ::onAutomaticExport,
                params = CreateFileParams(
                    fileName = "stt_records_automatic.csv",
                    type = FILE_TYPE_CSV,
                    notHandledCallback = ::onFileCreateError,
                ),
            )
        }
    }

    fun onRestoreClick(tag: String, params: BackupOptionsData.Restore) {
        restoreOptionsData = params
        router.navigate(
            StandardDialogParams(
                tag = tag,
                message = resourceRepo.getString(R.string.settings_dialog_message),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onExportCsvClick(tag: String) {
        router.navigate(DataExportSettingDialogParams(tag))
    }

    fun onImportCsvClick(tag: String) {
        router.navigate(
            StandardDialogParams(
                tag = tag,
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onImportCsvHelpClick() {
        HelpDialogParams(
            title = resourceRepo.getString(R.string.settings_import_csv),
            text = resourceRepo.getString(R.string.settings_import_csv_help)
                .let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) },
        ).let(router::navigate)
    }

    fun onExportIcsClick(tag: String) {
        router.navigate(DataExportSettingDialogParams(tag))
    }

    private suspend fun checkForAutomaticBackupError() {
        val automaticBackupError = prefsInteractor.getAutomaticBackupError()
        val automaticExportError = prefsInteractor.getAutomaticExportError()

        if (automaticBackupError || automaticExportError) {
            val backupString = resourceRepo.getString(R.string.message_automatic_backup_error)
                .takeIf { automaticBackupError }
            val exportString = resourceRepo.getString(R.string.message_automatic_export_error)
                .takeIf { automaticExportError }
            val hint = resourceRepo.getString(R.string.message_automatic_error_hint)
            val message = listOfNotNull(backupString, exportString, hint)
                .joinToString(separator = " ")

            router.show(
                SnackBarParams(
                    message = message,
                    duration = SnackBarParams.Duration.Indefinite,
                ),
            )
        }

        if (automaticBackupError) prefsInteractor.setAutomaticBackupError(false)
        if (automaticExportError) prefsInteractor.setAutomaticExportError(false)
    }

    private fun onSaveBackup(uriString: String?) {
        if (uriString == null) return
        val params = saveOptionsData ?: return
        executeFileWork {
            backupInteractor.saveBackupFile(uriString, params)
        }
    }

    private fun onAutomaticBackup(uriString: String?) {
        delegateScope.launch {
            if (uriString == null) {
                requestScreenUpdate()
                return@launch
            }

            val exportUri = prefsInteractor.getAutomaticExportUri()
            permissionRepo.releasePersistableUriPermissions(exportUri)

            if (permissionRepo.takePersistableUriPermission(uriString)) {
                prefsInteractor.setAutomaticBackupUri(uriString)
                automaticBackupInteractor.schedule()
            } else {
                prefsInteractor.setAutomaticBackupUri("")
                onFileCreateError()
            }

            requestScreenUpdate()
        }
    }

    private fun onAutomaticExport(uriString: String?) {
        delegateScope.launch {
            if (uriString == null) {
                requestScreenUpdate()
                return@launch
            }

            val backupUri = prefsInteractor.getAutomaticBackupUri()
            permissionRepo.releasePersistableUriPermissions(backupUri)

            if (permissionRepo.takePersistableUriPermission(uriString)) {
                prefsInteractor.setAutomaticExportUri(uriString)
                automaticExportInteractor.schedule()
            } else {
                prefsInteractor.setAutomaticExportUri("")
                onFileCreateError()
            }

            requestScreenUpdate()
        }
    }

    private fun disableAutomaticBackup() {
        delegateScope.launch {
            prefsInteractor.setAutomaticBackupUri("")
            prefsInteractor.setAutomaticBackupError(false)
            automaticBackupInteractor.cancel()
            requestScreenUpdate()
        }
    }

    private fun disableAutomaticExport() {
        delegateScope.launch {
            prefsInteractor.setAutomaticExportUri("")
            prefsInteractor.setAutomaticExportError(false)
            automaticExportInteractor.cancel()
            requestScreenUpdate()
        }
    }

    private fun onRestoreBackup(uriString: String?) {
        if (uriString == null) return
        val params = restoreOptionsData ?: return
        val restoreSettings = when (params) {
            is BackupOptionsData.Restore.Standard -> false
            is BackupOptionsData.Restore.WithSettings -> true
        }
        executeFileWork(
            doAfter = { if (restoreSettings) restartApp() },
        ) {
            backupInteractor.restoreBackupFile(uriString, params)
        }
    }

    private fun onPartialRestoreBackup(
        data: PartialBackupRestoreData,
    ) {
        val params = BackupOptionsData.Custom(data)
        executeFileWork {
            backupInteractor.partialRestoreBackupFile(params)
        }
    }

    private fun onPartialRestore(uriString: String?) {
        if (uriString == null) return
        executeFileWork(
            doAfter = { router.navigate(PartialRestoreParams) },
        ) {
            val (result, data) = backupInteractor.readBackupFileContent(uriString)
            partialBackupRestoreData = data
            result
        }
    }

    private fun onSaveCsvFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            csvExportInteractor.saveCsvFile(
                uriString = uriString,
                range = getRange(),
            )
        }
    }

    private fun onImportCsvFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            csvExportInteractor.importCsvFile(uriString)
        }
    }

    private fun onSaveIcsFile(uriString: String?) {
        if (uriString == null) return
        executeFileWork {
            icsExportInteractor.saveIcsFile(
                uriString = uriString,
                range = getRange(),
            )
        }
    }

    private fun requestFileWork(
        requestCode: String,
        work: (uriString: String?) -> Unit,
        params: ActionParams,
    ) {
        router.setResultListener(requestCode) { result ->
            work(result as? String)
        }
        router.execute(params)
    }

    // Need global scope or not cancelable scope.
    // Otherwise process will be stopped on navigation.
    private fun executeFileWork(
        doAfter: suspend () -> Unit = {},
        doWork: suspend () -> ResultCode,
    ) = delegateScope.launch {
        fileWorkRepo.inProgress.set(true)

        val resultCode = doWork()
        resultCode.message?.let(::showMessage)

        fileWorkRepo.inProgress.set(false)

        doAfter()
    }

    private fun onFileOpenError() {
        showMessage(resourceRepo.getString(R.string.settings_file_open_error))
    }

    private fun onFileCreateError() {
        showMessage(resourceRepo.getString(R.string.settings_file_create_error))
    }

    private fun showMessage(string: String) {
        val params = SnackBarParams(message = string)
        router.show(params)
    }

    private fun getFileNameTimeStamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    private fun getRange(): Range? {
        return dataExportSettingsResult?.range?.let {
            Range(
                timeStarted = it.rangeStart,
                timeEnded = it.rangeEnd,
            )
        }
    }

    private suspend fun loadAutomaticBackupEnabled(): Boolean {
        return prefsInteractor.getAutomaticBackupUri().isNotEmpty()
    }

    private suspend fun loadAutomaticExportEnabled(): Boolean {
        return prefsInteractor.getAutomaticExportUri().isNotEmpty()
    }

    private suspend fun requestScreenUpdate() {
        settingsDataUpdateInteractor.send()
    }

    private suspend fun restartApp() {
        // Delay for message to show.
        delay(1000)
        router.restartApp()
    }

    companion object {
        private const val FILE_TYPE_BIN = "application/x-binary"
        private const val FILE_TYPE_BIN_OPEN = "application/*"
        private const val FILE_TYPE_CSV = "text/csv"
        private const val FILE_TYPE_CSV_OPEN = "text/*"
        private const val FILE_TYPE_ICS = "application/ics"
    }
}