package info.skyblond.vazan.ui.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
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

    fun refresh() = viewModelScope.launch {
        loading = true
        val res = jimRepository.browse(currentParentId).mapNotNull {
            jimRepository.view(it)
        }
        synchronized(entryIds) {
            entryIds.clear()
            entryIds.addAll(res)
        }
        loading = false
    }
}