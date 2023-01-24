package info.skyblond.vazan.database

import androidx.paging.PagingSource
import androidx.room.*
import java.util.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNotes(vararg notes: Note)

    @Delete
    suspend fun deleteNotes(vararg notes: Note)

    @Query("DELETE FROM notes WHERE uuid = :uuid")
    suspend fun deleteNotesByUUID(uuid: UUID)

    @Query("SELECT DISTINCT uuid FROM notes")
    fun loadUniqueUUIDs(): PagingSource<Int, UUID>

    @Query("SELECT * FROM notes WHERE uuid = :uuid ORDER BY ctime DESC")
    fun loadNotesByUUID(uuid: UUID): PagingSource<Int, Note>

    @Query("SELECT COUNT(1) FROM notes WHERE uuid = :uuid")
    suspend fun countNotesByUUID(uuid: UUID): Int
}