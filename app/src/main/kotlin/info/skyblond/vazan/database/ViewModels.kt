package info.skyblond.vazan.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import java.util.*

class NoteViewModel(uuid: UUID) : ViewModel() {
    val notePager = Pager(config = PagingConfig(pageSize = 10)) {
        VazanDatabase.useDatabase { it.noteDao().loadNotesByUUID(uuid) }
    }.flow.cachedIn(viewModelScope)
}

class NoteUUIDViewModel : ViewModel() {
    val uuidPager = Pager(config = PagingConfig(pageSize = 10)) {
        VazanDatabase.useDatabase { it.noteDao().loadUniqueUUIDs() }
    }.flow.cachedIn(viewModelScope)
}

class SearchKeywordViewModel(keyword: String) : ViewModel() {
    val uuidPager = Pager(config = PagingConfig(pageSize = 10)) {
        VazanDatabase.useDatabase { it.noteDao().searchContentForUUID(keyword) }
    }.flow.cachedIn(viewModelScope)
}