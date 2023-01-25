package info.skyblond.vazan.database

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(vararg notes: Note)

    @Delete
    suspend fun deleteNotes(vararg notes: Note)

    @Query("DELETE FROM notes WHERE uuid = :uuid")
    suspend fun deleteNotesByUUID(uuid: UUID)

    @Query("SELECT DISTINCT uuid FROM notes ORDER BY uuid ASC")
    fun loadUniqueUUIDs(): PagingSource<Int, UUID>

    @Query("SELECT DISTINCT uuid FROM notes WHERE content like '%' || :highlight || '%' ORDER BY uuid ASC")
    fun searchContentForUUID(highlight: String): PagingSource<Int, UUID>

    @Query("SELECT * FROM notes WHERE uuid = :uuid ORDER BY ctime DESC")
    fun loadNotesByUUID(uuid: UUID): PagingSource<Int, Note>

    @Query("SELECT COUNT(1) FROM notes WHERE uuid = :uuid")
    suspend fun countNotesByUUID(uuid: UUID): Int

    @Query("SELECT * FROM notes")
    fun dumpAllNotes(): List<Note>
}