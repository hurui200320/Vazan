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
class KeywordSearchViewModel @Inject constructor(
    private val jimRepository: JimRepository,
) : ViewModel() {

    val keywordsList = mutableStateListOf("")
    val resultList = mutableStateListOf<JimEntry>()
    var showResult by mutableStateOf(false)
    var loading by mutableStateOf(false)

    fun search() = viewModelScope.launch {
        loading = true
        resultList.clear()
        showResult = true

        val keywords = keywordsList.filter { it.isNotBlank() }
        if (keywords.isNotEmpty()) {
            // search with keywordList and save result in resultList
            jimRepository.search(keywords).asSequence().forEach { entryId ->
                val e = jimRepository.view(entryId) ?: return@forEach
                resultList.add(e)
            }
        }

        loading = false
    }

}