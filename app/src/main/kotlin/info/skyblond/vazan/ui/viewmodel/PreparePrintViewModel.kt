package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.LabelEncoding
import info.skyblond.vazan.domain.repository.JimRepository
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PreparePrintViewModel @Inject constructor(
    private val jimRepository: JimRepository
) : ViewModel() {
    var labelType by mutableStateOf(LabelEncoding.LabelType.BOX)
    var labelValue by mutableStateOf("")
    var labelStatus by mutableStateOf("")

    private suspend fun refreshLabelStatus() {
        labelStatus = jimRepository.view(labelStatus)?.let { "IN_USE" } ?: "NEW"
    }

    private fun getLabelType(str: String): LabelEncoding.LabelType? = when (str.take(1)) {
        LabelEncoding.LabelType.BOX.prefix -> LabelEncoding.LabelType.BOX
        LabelEncoding.LabelType.ITEM.prefix -> LabelEncoding.LabelType.ITEM
        else -> null
    }

    fun generateLabel() = viewModelScope.launch {
        labelStatus = "UNKNOWN"
        withTimeout(5000) {
            do {
                labelValue = LabelEncoding.encodeToLabel(labelType, LabelEncoding.random(Random))
            } while (jimRepository.view(labelValue) != null)
            if (isActive) refreshLabelStatus()
        }
    }

    fun setLabel(str: String): Boolean {
        if (!isValidLabel(str)) return false
        labelType = getLabelType(str.uppercase()) ?: return false
        labelValue = str.uppercase()
        viewModelScope.launch { refreshLabelStatus() }
        return true
    }

    fun isValidLabel(str: String) = getLabelType(str.uppercase()) != null
            && str.length == 10
            && str.drop(1).uppercase().all { it in LabelEncoding.charset }

    init {
        generateLabel()
    }
}