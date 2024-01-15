package info.skyblond.vazan.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import info.skyblond.bencode.BEntry
import info.skyblond.bencode.BEntryType
import info.skyblond.bencode.decoder.BencodeDecoder
import info.skyblond.bencode.encoder.BencodeWriter
import kotlinx.coroutines.runBlocking
import java.io.Reader
import java.io.Writer

@Database(
    entities = [Config::class, Label::class], // TODO: remove Label
    version = 3, // TODO: 4
    autoMigrations = [
        AutoMigration(from = 2, to = 3, spec = AppDatabase.From2To3AutoMigration::class),
//        AutoMigration(from = 3, to = 4, spec = AppDatabase.From3To4AutoMigration::class),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    @RenameColumn("labels", "entry_id", "entity_id")
    class From2To3AutoMigration : AutoMigrationSpec

    @DeleteTable("labels")
    class From3To4AutoMigration : AutoMigrationSpec

    abstract val configDao: ConfigDao
    abstract val labelDao: LabelDao

    fun export(writer: Writer) {
        val bencodeWriter = BencodeWriter(writer)
        bencodeWriter.write(
            mapOf(
                "configs" to BEntry.BList(
                    configDao.listConfigs().asSequence().map { it.encodeToBEntry() }),
            )
        )
        bencodeWriter.flush()
    }

    private fun readList(decoder: BencodeDecoder): List<*> {
        decoder.startList()
        val list = mutableListOf<Any>()
        while (true) {
            when (decoder.nextType()) {
                BEntryType.EntityEnd -> break // this is the end of the list
                BEntry.BInteger -> list.add(decoder.readInteger())
                BEntry.BString -> list.add(decoder.readString())
                BEntry.BList -> list.add(readList(decoder))
                BEntry.BMap -> list.add(readMap(decoder))
            }
        }
        decoder.endEntity()
        return list
    }

    private fun readMap(decoder: BencodeDecoder): Map<String, *> {
        decoder.startMap()
        val map = mutableMapOf<String, Any>()
        while (decoder.nextType() != BEntryType.EntityEnd) {
            val key = decoder.readString()
            require(decoder.hasNext()) { "Invalid map: only has key, no value" }
            when (val t = decoder.nextType()) {
                BEntry.BInteger -> map[key] = decoder.readInteger()
                BEntry.BString -> map[key] = decoder.readString()
                BEntry.BList -> map[key] = readList(decoder)
                BEntry.BMap -> map[key] = readMap(decoder)
                else -> error("Invalid next type: $t")
            }
        }
        decoder.endEntity()
        return map
    }

    fun import(reader: Reader): Unit = runBlocking {
        // the import file should only contains 1 map
        // the map is <String, List<Map<String, *>>>
        //            <Type,   content of objects>
        val decoder = BencodeDecoder(reader)
        decoder.startMap()

        // not the end of the map
        while (decoder.nextType() != BEntryType.EntityEnd) {
            val type = decoder.readString()
            require(decoder.hasNext()) { "Invalid map: only has key, no value" }
            when (type) {
                "configs" -> {
                    decoder.startList()
                    while (decoder.nextType() != BEntryType.EntityEnd) {
                        val map = Config.fromMap(readMap(decoder))
                        map?.let { configDao.insertOrUpdateConfig(it) }
                    }
                    decoder.endEntity()
                }

                else -> error("Unknown type: $type")
            }
        }
        // end of the map
        decoder.endEntity()
    }
}
