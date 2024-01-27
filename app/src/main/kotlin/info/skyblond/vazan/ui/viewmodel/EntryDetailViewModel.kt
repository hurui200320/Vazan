package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.model.JimMeta
import info.skyblond.vazan.domain.repository.JimRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val jimRepository: JimRepository
) : ViewModel() {
    var entry by mutableStateOf(JimEntry("", "", "", 0, "", "", emptyList()))

    var confirmDeleteEntry by mutableStateOf(false)
    var confirmDeleteTag: JimMeta? by mutableStateOf(null)


    fun getEntry(entryId: String, onFailure: () -> Unit) = viewModelScope.launch {
        val t = jimRepository.view(entryId)
        if (t != null) entry = t
        else onFailure()
    }


    fun updateEntry(
        fieldName: String,
        value: String?,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit
    ) = viewModelScope.launch {
        val t = jimRepository.updateEntry(entry.entryId, listOf(fieldName to value))
        if (t != null) entry = t.also { onSuccess() }
        else onFailure()
    }

    fun deleteEntry(onSuccess: () -> Unit, onFailure: () -> Unit) = viewModelScope.launch {
        jimRepository.deleteEntry(entry.entryId)?.let { onSuccess() } ?: onFailure()
    }

    fun deleteMetadata(tag: JimMeta, onSuccess: () -> Unit, onFailure: () -> Unit) =
        viewModelScope.launch {
            jimRepository.deleteMeta(entry.entryId, tag.name)?.let { onSuccess() } ?: onFailure()
            getEntry(entry.entryId) {}
        }

    fun createMeta(
        newEntryName: String, type: String,
        onSuccess: () -> Unit, onFailure: () -> Unit
    ) = viewModelScope.launch {
        val t = jimRepository.createMeta(entry.entryId, newEntryName.trim { it == '_' }, type)
        if (t != null) entry = t.also { onSuccess() }
        else onFailure()
    }

    fun updateMeta(metaName: String, fieldName: String, value: String, onFailure: () -> Unit) =
        viewModelScope.launch {
            jimRepository.updateMeta(entry.entryId, metaName, listOf(fieldName to value))
                ?.let { getEntry(entry.entryId) {} } ?: onFailure()
        }

}