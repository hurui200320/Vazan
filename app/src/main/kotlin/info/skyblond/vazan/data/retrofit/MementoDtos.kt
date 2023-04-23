package info.skyblond.vazan.data.retrofit

import com.squareup.moshi.Json

data class ListLibrariesDto(
    @field:Json(name = "libraries")
    val libraries: List<ListLibrariesEntry>
) {
    data class ListLibrariesEntry(
        @field:Json(name = "id")
        val id: String,
        @field:Json(name = "name")
        val name: String,
        @field:Json(name = "owner")
        val owner: String,
        @field:Json(name = "createdTime")
        val createdTime: String,
        @field:Json(name = "modifiedTime")
        val modifiedTime: String
    )
}

data class GetLibraryDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "owner")
    val owner: String,
    @field:Json(name = "createdTime")
    val createdTime: String,
    @field:Json(name = "modifiedTime")
    val modifiedTime: String,
    @field:Json(name = "revision")
    val revision: Int,
    @field:Json(name = "size")
    val size: Int,
    @field:Json(name = "fields")
    val fields: List<LibraryField>
) {
    data class LibraryField(
        @field:Json(name = "id")
        val id: Int,
        @field:Json(name = "type")
        val type: String,
        @field:Json(name = "name")
        val name: String,
        @field:Json(name = "role")
        val role: String?,
        @field:Json(name = "order")
        val order: Int
    )
}

data class EntryDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "author")
    val author: String,
    @field:Json(name = "createdTime")
    val createdTime: String,
    @field:Json(name = "modifiedTime")
    val modifiedTime: String,
    @field:Json(name = "revision")
    val revision: Int,
    @field:Json(name = "status")
    val status: String,
    @field:Json(name = "size")
    val size: Int,
    @field:Json(name = "fields")
    val fields: List<EntryField>
) {
    data class EntryField(
        @field:Json(name = "id")
        val id: Int,
        @field:Json(name = "value")
        val value: Any?
    )
}

data class ListEntriesByLibraryIdDto(
    @field:Json(name = "entries")
    val entries: List<EntryDto>,
    @field:Json(name = "nextPageToken")
    val nextPageToken: String?,
    @field:Json(name = "revision")
    val revision: Int
)

data class UpdateEntryDto(
    @field:Json(name = "fields")
    val fields: List<EntryDto.EntryField>
)