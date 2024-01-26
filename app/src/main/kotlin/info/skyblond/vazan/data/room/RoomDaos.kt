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
