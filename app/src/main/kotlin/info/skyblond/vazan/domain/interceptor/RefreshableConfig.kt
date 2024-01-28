package info.skyblond.vazan.domain.interceptor

import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty

class RefreshableConfig(
    private val configRepository: ConfigRepository,
    private val key: SettingsKey
) {
    private var cache: String = ""
    private var lastUpdate: Long = 0

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if (System.currentTimeMillis() - lastUpdate > 5000)
            runBlocking {
                cache = configRepository.getConfigByKey(key)?.value ?: ""
                lastUpdate = System.currentTimeMillis()
            }
        return cache
    }
}