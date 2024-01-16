package info.skyblond.vazan.data.retrofit

@Suppress("PropertyName")
data class JimRequestDto(
    val request_id: String,
    val timestamp: Long,
    val cmd: String,
    val params: List<*> = emptyList<Any>()
)

@Suppress("PropertyName")
data class JimStringListResponseDto(
    val request_id: String,
    val err: String?,
    val result: List<String> = emptyList()
)

@Suppress("PropertyName")
data class JimEntryResponseDto(
    val request_id: String,
    val err: String?,
    val result: JimEntryDto
)

@Suppress("PropertyName")
data class JimMetaResponseDto(
    val request_id: String,
    val err: String?,
    val result: JimMetaDto
)

@Suppress("PropertyName")
data class JimEntryDto(
    val entry_id: String,
    val entry_type: String,
    val entry_parent_id: String?,
    val entry_children_count: Long,
    val entry_name: String,
    val entry_note: String,
    val entry_meta_list: List<JimMetaDto>
)

@Suppress("PropertyName")
data class JimMetaDto(
    val meta_name: String,
    val meta_type: String,
    val meta_need_value: Boolean,
    val meta_value: String
)