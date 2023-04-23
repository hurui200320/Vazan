package info.skyblond.vazan.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.room.Config
import info.skyblond.vazan.data.room.Label
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.LabelRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val mementoRepository: MementoRepository,
    private val configRepository: ConfigRepository,
    private val labelRepository: LabelRepository
) : ViewModel() {
    private val tag = "SyncViewModel"

    lateinit var showToast: (String) -> Unit

    var lastSyncTimestamp by mutableStateOf(0L)
    var isCurrentlySyncing by mutableStateOf(false)
    var currentSyncingVersion by mutableStateOf(0L)
    var syncedBoxCount by mutableStateOf(0L)
    var syncedItemCount by mutableStateOf(0L)

    private fun updateLastSyncTimestamp() = viewModelScope.launch {
        lastSyncTimestamp =
            configRepository.getConfigByKey(SettingsKey.MEMENTO_SYNC_VERSION.key)?.value?.toLong()
                ?: 0L
    }

    init {
        updateLastSyncTimestamp()
    }

    private suspend fun syncLibrary(
        libraryIdSettingsKey: SettingsKey,
        fieldIdSettingsKey: SettingsKey,
        count: () -> Unit
    ) {
        val libId = configRepository.getConfigByKey(libraryIdSettingsKey.key)?.value ?: kotlin.run {
            showToast("Library ID not found, please check your settings")
            return
        }
        val fieldId = configRepository.getConfigByKey(fieldIdSettingsKey.key)?.value?.toIntOrNull()
            ?: kotlin.run {
                showToast("Field ID not found, please check your settings")
                return
            }
        mementoRepository.listEntriesByLibraryId(libId, 5000, fieldId.toString()).collect { r ->
            r.forEach { entry ->
                val labelId =
                    entry.fields.find { it.id == fieldId }?.value as? String ?: return@forEach
                if (labelId.isBlank()) return@forEach
                val entryId = entry.id
                val label = Label(
                    labelId = labelId, status = Label.Status.IN_USE,
                    version = currentSyncingVersion, entryId = entryId
                )
                labelRepository.insertOrUpdateLabel(label)
                count()
            }
        }
    }

    fun sync() {
        synchronized(this) {
            if (isCurrentlySyncing) return
            isCurrentlySyncing = true
        }
        currentSyncingVersion = System.currentTimeMillis()
        syncedBoxCount = 0
        syncedItemCount = 0
        viewModelScope.launch {
            Log.i(tag, "Syncing boxes")
            syncLibrary(
                SettingsKey.MEMENTO_BOX_LIBRARY_ID,
                SettingsKey.MEMENTO_BOX_FIELD_ID
            ) { syncedBoxCount++ }
            Log.i(tag, "Boxes synced")

            // wait 2s to ease the rate limit
            delay(2000)
            Log.i(tag, "Syncing items")
            syncLibrary(
                SettingsKey.MEMENTO_ITEM_LIBRARY_ID,
                SettingsKey.MEMENTO_ITEM_FIELD_ID
            ) { syncedItemCount++ }
            Log.i(tag, "Items synced")

            // update version
            Log.i(tag, "Updating database")
            configRepository.insertOrUpdateConfig(
                Config(
                    key = SettingsKey.MEMENTO_SYNC_VERSION.key,
                    value = currentSyncingVersion.toString()
                )
            )
            // delete all unseen IN_USE labels
            labelRepository.deleteOldLabelsByStatus(
                Label.Status.IN_USE,
                currentSyncingVersion
            )
            Log.i(tag, "Syncing finished")
            // update ui
            updateLastSyncTimestamp()
            synchronized(this) {
                isCurrentlySyncing = false
            }
        }
    }

    fun clearPrintedLabels(type: Label.Status) {
        synchronized(this) {
            if (isCurrentlySyncing) return
            isCurrentlySyncing = true
        }
        viewModelScope.launch {
            labelRepository.deleteLabelByStatus(type)
            showToast("All $type labels are deleted")
            synchronized(this) {
                isCurrentlySyncing = false
            }
        }
    }
}