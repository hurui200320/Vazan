package info.skyblond.vazan.ui.viewmodel

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.repository.JimRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val jimRepository: JimRepository
) : ViewModel() {
    var status by mutableStateOf("Processing...")
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

        val t = jimRepository.createEntry(barcode)

        if (t != null) { // ok
            delay(1500)
            soundOk()
            status = "OK"
            delay(2500)
        } else {
            delay(1000)
            showError("Failed to add")
        }
        callBack()
    }
}