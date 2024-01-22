package info.skyblond.vazan.ui.viewmodel

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.retrofit.EntryDto
import info.skyblond.vazan.data.retrofit.UpdateEntryDto
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.JimRepository
import info.skyblond.vazan.domain.repository.LabelRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val jimRepository: JimRepository
) : ViewModel() {
    var entry by mutableStateOf(JimEntry("", "", "", 0, "", "", emptyList()))

    var confirmDelete by mutableStateOf(false)

    fun getEntry(entryId: String, onFailure: () -> Unit) = viewModelScope.launch {
        val t = jimRepository.view(entryId)
        if (t != null) entry = t
        else onFailure()
    }

    fun updateEntry(fieldName: String, value: String?, onFailure: () -> Unit) =
        viewModelScope.launch {
            val t = jimRepository.updateEntry(entry.entryId, listOf(fieldName to value))
            if (t != null) entry = t
            else onFailure()
        }

}