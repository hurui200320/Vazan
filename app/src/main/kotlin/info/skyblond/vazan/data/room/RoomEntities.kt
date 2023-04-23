package info.skyblond.vazan.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import info.skyblond.bencode.BEntry
import info.skyblond.bencode.encoder.BencodeEncodable
import java.math.BigInteger

@Entity(
    tableName = "configs",
    primaryKeys = ["config_key"],
    indices = [Index("config_key", unique = true)]
)
data class Config(
    @ColumnInfo(name = "config_key") val key: String,
    @ColumnInfo(name = "config_value") val value: String,
) : BencodeEncodable {

    companion object {
        fun fromMap(map: Map<String, *>): Config? {
            val key = map["config_key"] as? String ?: return null
            val value = map["config_value"] as? String ?: return null
            return Config(key = key, value = value)
        }
    }

    override fun encodeToBEntry(): BEntry = BEntry.BMap(
        buildMap {
            put("config_key") { BEntry.BString(key) }
            put("config_value") { BEntry.BString(value) }
        }
    )
}

@Entity(
    tableName = "labels",
    primaryKeys = ["label_id"],
    indices = [
        Index("label_id", unique = true),
        Index("entry_id"),
        Index("label_status", "version"),
    ]
)
data class Label(
    @ColumnInfo(name = "label_id") val labelId: String,
    @ColumnInfo(name = "label_status") val status: Status,
    @ColumnInfo(name = "version") val version: Long,
    @ColumnInfo(name = "entry_id") val entryId: String?,
) : BencodeEncodable {

    enum class Status {
        IN_USE, PRINTED
    }

    companion object {
        fun fromMap(map: Map<String, *>): Label? {
            val labelId = map["label_id"] as? String ?: return null
            val status = (map["label_status"] as? String)?.let { Status.valueOf(it) } ?: return null
            val version = map["version"] as? BigInteger ?: return null
            val entryId = map["entry_id"] as? String
            return Label(
                labelId = labelId, status = status,
                version = version.toLong(), entryId = entryId
            )
        }
    }

    override fun encodeToBEntry(): BEntry = BEntry.BMap(
        buildMap {
            put("label_id") { BEntry.BString(labelId) }
            put("label_status") { BEntry.BString(status.name) }
            put("version") { BEntry.BInteger(version.toBigInteger()) }
            entryId?.let { put("entry_id") { BEntry.BString(it) } }
        }
    )
}