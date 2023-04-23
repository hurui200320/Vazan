package info.skyblond.vazan.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class ConfigDao {

    @Query("SELECT * FROM configs")
    abstract fun listConfigs(): List<Config>

    @Query("SELECT * FROM configs WHERE config_key = :key")
    abstract suspend fun getConfigByKey(key: String): Config?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdateConfig(config: Config)

    @Delete
    abstract suspend fun deleteConfig(config: Config)
}

@Dao
abstract class LabelDao {
    @Query("SELECT * FROM labels")
    abstract fun listLabels(): List<Label>

    @Query("SELECT * FROM labels WHERE label_id = :labelId")
    abstract suspend fun getLabelById(labelId: String): Label?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdateLabel(label: Label)

    @Delete
    abstract suspend fun deleteLabel(label: Label)

    @Query("DELETE FROM labels WHERE label_status = :status")
    abstract suspend fun deleteLabelByStatus(status: Label.Status)

    @Query("DELETE FROM labels WHERE label_status = :status AND version != :latestVersion")
    abstract suspend fun deleteOldLabelsByStatus(status: Label.Status, latestVersion: Long)
}