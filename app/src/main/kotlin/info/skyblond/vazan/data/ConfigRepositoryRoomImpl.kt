package info.skyblond.vazan.data

import info.skyblond.vazan.data.room.Config
import info.skyblond.vazan.data.room.ConfigDao
import info.skyblond.vazan.domain.repository.ConfigRepository
import javax.inject.Inject

class ConfigRepositoryRoomImpl @Inject constructor(
    private val dao: ConfigDao
) : ConfigRepository {
    override suspend fun getConfigByKey(key: String): Config? = dao.getConfigByKey(key)

    override suspend fun insertOrUpdateConfig(config: Config) = dao.insertOrUpdateConfig(config)

    override suspend fun deleteConfig(config: Config) = dao.deleteConfig(config)
}