package info.skyblond.vazan.database

import android.util.JsonReader
import android.util.JsonWriter
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
) {
    companion object {
        fun readJson(jsonReader: JsonReader): Note {
            val map = mutableMapOf<String, Any>()
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                when (val name = jsonReader.nextName()) {
                    "uuid" -> map[name] = jsonReader.nextString()
                    "ctime" -> map[name] = jsonReader.nextLong()
                    "content" -> map[name] = jsonReader.nextString()
                }
            }
            jsonReader.endObject()
            return Note(
                uuid = UUID.fromString(map["uuid"] as String),
                createTime = map["ctime"] as Long,
                content = map["content"] as String
            )
        }
    }

    fun writeJson(jsonWriter: JsonWriter) {
        jsonWriter.beginObject()
        // uuid
        jsonWriter.name("uuid")
        jsonWriter.value(uuid.toString())
        // create time
        jsonWriter.name("ctime")
        jsonWriter.value(createTime)
        // content
        jsonWriter.name("content")
        jsonWriter.value(content)
        jsonWriter.endObject()
    }
}