package info.skyblond.vazan.ui.viewmodel

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.repository.JimRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QuickMoveToViewModel @Inject constructor(
    private val jimRepository: JimRepository
) : ViewModel() {
    var status by mutableStateOf("Processing...")
    var targetEntry: JimEntry by mutableStateOf(JimEntry("", "", null, 0, "", "", emptyList()))
    var currentLabel by mutableStateOf("")

    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

    private suspend fun showError(str: String) {
        withContext(Dispatchers.Main) { toneGen.startTone(ToneGenerator.TONE_CDMA_PIP) }
        status = "ERROR: $str"
        delay(5000)
    }

    private suspend fun soundOk() = withContext(Dispatchers.Main) {
        toneGen.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT)
    }

    fun processLabel(barcode: String, callBack: () -> Unit) = viewModelScope.launch {
        status = "Processing..."
        currentLabel = barcode

        val currentEntry = jimRepository.view(barcode) ?: run {
            showError("Entry not found")
            return@launch
        }

        when (targetEntry.type) {
            "LOCATION" -> {} // no limit when move into locations
            "BOX" -> if (currentEntry.type == "LOCATION") {
                showError("Cannot move location into box")
                return@launch
            }

            else -> {
                showError("Internal error")
                return@launch
            }
        }

        // Must not add ourself as parent
        val t = if (currentEntry.entryId == targetEntry.entryId) null
        else jimRepository.updateEntry(
            currentEntry.entryId,
            listOf("parent_id" to targetEntry.entryId)
        )

        if (t != null) { // ok
            delay(1500)
            soundOk()
            status = "OK"
            delay(2500)
        } else {
            delay(1000)
            showError("Failed to move")
        }
        callBack()
    }

    fun checkEntry(targetEntityId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) =
        viewModelScope.launch {
            targetEntry = jimRepository.view(targetEntityId) ?: kotlin.run {
                onFailure("Target entry not found")
                return@launch
            }
            if (targetEntry.type == "ITEM") {
                onFailure("Target entry cannot be an ITEM")
                return@launch
            }
            onSuccess()
        }

}