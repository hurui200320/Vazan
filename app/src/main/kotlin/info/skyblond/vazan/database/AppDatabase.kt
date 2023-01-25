package info.skyblond.vazan.database

import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.runBlocking


@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

object VazanDatabase {
    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = Room.databaseBuilder(context, AppDatabase::class.java, "vazan").build()
    }

    fun <R> useDatabase(block: suspend (AppDatabase) -> R): R =
        synchronized(db) {
            runBlocking {
                block.invoke(db)
            }
        }

    fun dumpJson(jsonWriter: JsonWriter): Unit = useDatabase { db ->
        jsonWriter.beginObject()
        // array of note
        jsonWriter.name("notes")
        jsonWriter.beginArray()
        db.noteDao().dumpAllNotes().forEach { it.writeJson(jsonWriter) }
        jsonWriter.endArray()

        jsonWriter.endObject()
    }

    fun fromJson(jsonReader: JsonReader): Unit = useDatabase { db ->
        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "notes" -> {
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        val note = Note.readJson(jsonReader)
                        db.noteDao().insertNotes(note)
                    }
                    jsonReader.endArray()
                }
            }
        }

        jsonReader.endObject()
    }
}
