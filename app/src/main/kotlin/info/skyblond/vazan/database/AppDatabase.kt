package info.skyblond.vazan.database

import android.content.Context
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
}
