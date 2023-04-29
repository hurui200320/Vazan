package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.LabelEncoding
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.LabelRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickScanViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val labelRepo: LabelRepository,
    private val mementoRepository: MementoRepository
) : ViewModel() {
    lateinit var showToast: (String) -> Unit
    lateinit var scanner: GmsBarcodeScanner

    val locationList = mutableStateListOf<Pair<String, String>>()
    var showSelectLocationDialog by mutableStateOf(false)

    fun listLocation() {
        if (locationList.isNotEmpty()) return
        viewModelScope.launch {
            val libId =
                configRepo.getConfigByKey(SettingsKey.MEMENTO_LOCATION_LIBRARY_ID)?.value
                    ?: kotlin.run {
                        showToast("Library ID not found, please check your settings")
                        return@launch
                    }
            val fieldId =
                configRepo.getConfigByKey(SettingsKey.MEMENTO_LOCATION_FIELD_ID)?.value?.toIntOrNull()
                    ?: kotlin.run {
                        showToast("Field ID not found, please check your settings")
                        return@launch
                    }
            locationList.clear()
            mementoRepository.listEntriesByLibraryId(libId, 5000, fieldId.toString()).collect { r ->
                locationList.addAll(
                    r.map { e -> e.fields.find { it.id == fieldId }?.value to e.id }
                        .filter { p -> p.first as? String != null }
                        .map { p -> (p.first as String) to p.second }
                )
            }
        }
    }

    fun processMoveToBox(str: String, callBack: (String) -> Unit) = viewModelScope.launch {
        if (str.take(1) != LabelEncoding.LabelType.BOX.prefix) {
            showToast("Please scan only box label"); return@launch
        }
        val label = labelRepo.getLabelById(str) ?: kotlin.run {
            showToast("Label not found, please re-sync database"); return@launch
        }
        val entryId = label.entityId ?: kotlin.run {
            showToast("Entry not found, please re-sync database"); return@launch
        }
        showToast("Moving to box ${label.labelId}(${entryId})")
        callBack(entryId)
    }
}