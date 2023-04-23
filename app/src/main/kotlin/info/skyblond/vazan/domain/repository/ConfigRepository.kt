package info.skyblond.vazan.domain.repository

import info.skyblond.vazan.data.room.Config
import info.skyblond.vazan.domain.LabelEncoding.LabelType
import info.skyblond.vazan.domain.SettingsKey

interface ConfigRepository {
    suspend fun getConfigByKey(key: String): Config?

    suspend fun insertOrUpdateConfig(config: Config)

    suspend fun deleteConfig(config: Config)

    private suspend fun getConfigOrBlank(settingsKey: SettingsKey): String =
        getConfigByKey(settingsKey.key)?.value ?: ""

    /**
     * Get the item library id (box or item), and the parent location and box fields.
     *
     * Item library id might be blank if config is not set.
     * Fields id might be null if config is not set.
     *
     * @return Triple(itemLibId, itemParentLocationField, itemParentBoxField)
     * */
    suspend fun resolveConfig(prefix: String): Triple<String, Int?, Int?> {
        val itemLibId = when (prefix) {
            LabelType.BOX.prefix -> getConfigOrBlank(SettingsKey.MEMENTO_BOX_LIBRARY_ID)
            LabelType.ITEM.prefix -> getConfigOrBlank(SettingsKey.MEMENTO_ITEM_LIBRARY_ID)
            else -> ""
        }
        val itemParentLocationField = when (prefix) {
            LabelType.BOX.prefix -> getConfigOrBlank(SettingsKey.MEMENTO_BOX_PARENT_LOCATION_FIELD_ID).toIntOrNull()
            LabelType.ITEM.prefix -> getConfigOrBlank(SettingsKey.MEMENTO_ITEM_PARENT_LOCATION_FIELD_ID).toIntOrNull()
            else -> null
        }
        val itemParentBoxField = when (prefix) {
            LabelType.BOX.prefix -> getConfigOrBlank(SettingsKey.MEMENTO_BOX_PARENT_BOX_FIELD_ID).toIntOrNull()
            LabelType.ITEM.prefix -> getConfigOrBlank(SettingsKey.MEMENTO_ITEM_PARENT_BOX_FIELD_ID).toIntOrNull()
            else -> null
        }
        return Triple(itemLibId, itemParentLocationField, itemParentBoxField)
    }
}