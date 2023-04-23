package info.skyblond.vazan.domain.repository

import info.skyblond.vazan.data.retrofit.EntryDto
import info.skyblond.vazan.data.retrofit.UpdateEntryDto
import info.skyblond.vazan.domain.model.LibraryBrief
import info.skyblond.vazan.domain.model.LibraryField
import kotlinx.coroutines.flow.Flow

interface MementoRepository {
    suspend fun listLibraries(): List<LibraryBrief>

    suspend fun getLibraryFields(libraryId: String): List<LibraryField>

    /**
     * The [fields] is fields id list.
     *
     * For example, if only want to list values for id 1 and 2,
     * this field should be "1,2"
     * */
    suspend fun listEntriesByLibraryId(
        libraryId: String,
        pageSize: Int? = null,
        fields: String? = null,
        startRevision: Int? = null,
    ): Flow<List<EntryDto>>

    suspend fun updateEntryByLibraryIdAndEntryId(
        libraryId: String,
        entryId: String,
        updateEntryDto: UpdateEntryDto,
    ): EntryDto
}