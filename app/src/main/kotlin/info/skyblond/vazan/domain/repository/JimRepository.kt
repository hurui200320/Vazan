package info.skyblond.vazan.domain.repository

import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.model.JimMeta

interface JimRepository {
    suspend fun browse(parentId: String?): List<String>

    suspend fun search(keywords: List<String>): List<String>

    suspend fun view(entryId: String): JimEntry?

    suspend fun createEntry(
        entryId: String,
        type: String? = null,
        parentId: String? = null,
        name: String = "",
        note: String = ""
    ): JimEntry?

    suspend fun updateEntry(
        entryId: String,
        fieldValuePairs: List<Pair<String, String?>>
    ): JimEntry?

    suspend fun deleteEntry(entryId: String): JimEntry?

    suspend fun createMeta(
        entryId: String,
        name: String,
        type: String,
        value: String = ""
    ): JimEntry?

    suspend fun updateMeta(entryId: String, name: String, fieldValuePairs: List<Pair<String, String?>>): JimEntry?
    suspend fun deleteMeta(entryId: String, name: String): JimMeta?
}