package info.skyblond.vazan.domain.model

import info.skyblond.vazan.data.retrofit.GetLibraryDto
import info.skyblond.vazan.data.retrofit.ListLibrariesDto

data class LibraryBrief(
    val id: String,
    val name: String,
    val owner: String
) {
    companion object {
        fun fromDto(obj: ListLibrariesDto.ListLibrariesEntry): LibraryBrief = LibraryBrief(
            id = obj.id, name = obj.name, owner = obj.owner
        )
    }
}

data class LibraryField(
    val id: Int,
    val type: String,
    val name: String,
) {
    companion object {
        fun fromDto(obj: GetLibraryDto.LibraryField): LibraryField = LibraryField(
            id = obj.id, type = obj.type, name = obj.name
        )
    }
}