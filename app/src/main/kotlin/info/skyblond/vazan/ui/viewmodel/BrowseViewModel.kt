package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.repository.JimRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val jimRepository: JimRepository,
) : ViewModel() {

    var currentParentId: String? by mutableStateOf(null)
    val entryIds = mutableStateListOf<JimEntry>()
    var loading by mutableStateOf(false)

    private var currentEntry: JimEntry? by mutableStateOf(null)

    fun refresh() = viewModelScope.launch {
        loading = true
        val res = jimRepository.browse(currentParentId).mapNotNull {
            jimRepository.view(it)
        }
        synchronized(entryIds) {
            entryIds.clear()
            entryIds.addAll(res)
        }
        currentEntry = currentParentId?.let { jimRepository.view(it) }
        loading = false
    }

    fun getSubTypeOptions() = buildList {
        // only location can have sub locations
        if (currentParentId == null || currentEntry?.type == "LOCATION")
            add("LOCATION")
        // location and box can have sub box
        if (currentParentId == null || currentEntry?.type == "LOCATION" || currentEntry?.type == "BOX")
            add("BOX")
        // location and box can have sub item
        if (currentParentId == null || currentEntry?.type == "LOCATION" || currentEntry?.type == "BOX")
            add("ITEM")
    }

    fun guessType(entryId: String, oldType: String): String = when (entryId.firstOrNull()) {
        'L' -> "LOCATION"
        'B' -> "BOX"
        'I' -> "ITEM"
        else -> oldType
    }

    fun createEntry(
        newEntryId: String, type: String,
        onSuccess: (JimEntry) -> Unit, onFailure: () -> Unit
    ) = viewModelScope.launch {
        val t = jimRepository.createEntry(newEntryId.trim(), type, currentParentId)
        t?.let { onSuccess(it) } ?: onFailure()
    }
}