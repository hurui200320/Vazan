package info.skyblond.vazan.domain.model

import info.skyblond.vazan.data.retrofit.JimEntryDto
import info.skyblond.vazan.data.retrofit.JimMetaDto
import info.skyblond.vazan.domain.model.JimMeta.Companion.toModel

data class JimEntry(
    val entryId: String,
    val type: String,
    val parentId: String?,
    val childrenCount: Long,
    val name: String,
    val note: String,
    val metaList: List<JimMeta>
) {
    companion object {
        fun JimEntryDto.toModel() = JimEntry(
            entryId = entry_id,
            type = entry_type,
            parentId = entry_parent_id,
            childrenCount = entry_children_count,
            name = entry_name,
            note = entry_note,
            metaList = entry_meta_list.map { it.toModel() }
        )
    }
}

data class JimMeta(
    val name: String,
    val type: String,
    val needValue: Boolean,
    val value: String
) {
    companion object {
        fun JimMetaDto.toModel() = JimMeta(
            name = meta_name,
            type = meta_type,
            needValue = meta_need_value,
            value = meta_value
        )
    }
}