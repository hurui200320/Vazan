package info.skyblond.vazan.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import java.util.*

@Entity(
    tableName = "notes",
    primaryKeys = ["uuid", "ctime"],
    indices = [Index("uuid"), Index("uuid", "ctime", unique = true)]
)
data class Note(
    @ColumnInfo(name = "uuid") val uuid: UUID,
    @ColumnInfo(name = "ctime") val createTime: Long,
    @ColumnInfo(name = "content") val content: String,
)