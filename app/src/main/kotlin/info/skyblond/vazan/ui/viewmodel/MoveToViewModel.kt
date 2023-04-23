package info.skyblond.vazan.ui.viewmodel

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.retrofit.EntryDto
import info.skyblond.vazan.data.retrofit.UpdateEntryDto
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.LabelRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MoveToViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val labelRepo: LabelRepository,
    private val mementoRepository: MementoRepository
) : ViewModel() {
    var status by mutableStateOf("Processing...")
    var targetEntityId by mutableStateOf("")
    var targetName by mutableStateOf("")
    var targetType by mutableStateOf("")
    var currentLabel by mutableStateOf("")

    lateinit var scanner: GmsBarcodeScanner

    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

    private fun getUpdateEntryDto(parentLocationField: Int, parentBoxField: Int): UpdateEntryDto =
        when (targetType) {
            "location" -> UpdateEntryDto(
                listOf(
                    EntryDto.EntryField(parentLocationField, targetEntityId),
                    EntryDto.EntryField(parentBoxField, null),
                )
            )

            "box" -> UpdateEntryDto(
                listOf(
                    EntryDto.EntryField(parentLocationField, null),
                    EntryDto.EntryField(parentBoxField, targetEntityId),
                )
            )

            else -> error("Invalid target type. Expect location or box, but got $targetType")
        }

    private suspend fun showError(str: String) {
        withContext(Dispatchers.Main) { toneGen.startTone(ToneGenerator.TONE_CDMA_PIP) }
        status = "ERROR: $str"
        delay(5000)
    }

    private suspend fun soundOk() = withContext(Dispatchers.Main) {
        toneGen.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT)
    }

    fun processLabel(str: String, callBack: () -> Unit) = viewModelScope.launch {
        status = "Processing..."
        currentLabel = str
        val label = labelRepo.getLabelById(str)
        if (label?.entryId != null) {
            if (targetType != "box" || label.entryId != targetEntityId) {
                val (libId, parentLocationField, parentBoxField) = configRepo.resolveConfig(
                    label.labelId.take(1)
                )
                if (libId.isNotBlank() && parentLocationField != null && parentBoxField != null) {
                    try {
                        mementoRepository.updateEntryByLibraryIdAndEntryId(
                            libId,
                            label.entryId,
                            getUpdateEntryDto(parentLocationField, parentBoxField)
                        )
                        delay(1500)
                        soundOk()
                        status = "OK"
                        delay(2500)
                    } catch (t: Throwable) {
                        delay(1000)
                        showError(t.message ?: "unknown error")
                    }
                } else {
                    showError("invalid settings")
                }
            } else {
                showError("loop detected")
            }
        } else {
            showError("Label not found")
        }
        callBack()
    }

}