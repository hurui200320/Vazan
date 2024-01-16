package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.room.Label
import info.skyblond.vazan.domain.LabelEncoding
import info.skyblond.vazan.domain.repository.LabelRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PreparePrintActivity @Inject constructor(
    private val labelRepository: LabelRepository
) : ViewModel() {
    var labelType by mutableStateOf(LabelEncoding.LabelType.BOX)
    var labelValue by mutableStateOf("")
    var labelStatus by mutableStateOf("")

    private suspend fun refreshLabelStatus() {
        labelStatus = labelRepository.getLabelById(labelValue)?.status?.name ?: "NEW"
    }

    private fun getLabelType(str: String): LabelEncoding.LabelType? = when (str.take(1)) {
        LabelEncoding.LabelType.BOX.prefix -> LabelEncoding.LabelType.BOX
        LabelEncoding.LabelType.ITEM.prefix -> LabelEncoding.LabelType.ITEM
        else -> null
    }

    fun generateLabel() = viewModelScope.launch {
        var label: String
        do {
            label = LabelEncoding.encodeToLabel(labelType, LabelEncoding.random(Random))
        } while (labelRepository.getLabelById(label) != null)
        labelValue = label
        refreshLabelStatus()
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

    fun afterPrintLabel(str: String) = viewModelScope.launch {
        // for existing label, it's either printed or in_use, no need to update them
        if (labelRepository.getLabelById(str) == null) {
            // this is new label, mark it as printed
            labelRepository.insertOrUpdateLabel(
                Label(
                    labelId = str,
                    status = Label.Status.PRINTED,
                    version = 0,
                    entityId = null
                )
            )
        }
        if (str == labelValue) refreshLabelStatus()
    }

    init {
        generateLabel()
    }

}