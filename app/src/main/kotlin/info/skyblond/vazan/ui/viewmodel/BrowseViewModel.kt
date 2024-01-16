package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.repository.ConfigRepository
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
        entryIds.clear()
        val res = jimRepository.browse(currentParentId).mapNotNull {
            jimRepository.view(it)
        }
        loading = false
        entryIds.addAll(res)
    }
}