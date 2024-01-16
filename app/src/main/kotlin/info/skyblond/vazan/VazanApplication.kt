package info.skyblond.vazan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import info.skyblond.vazan.domain.SettingsKey

@HiltAndroidApp
class VazanApplication : Application() {
    init {
        // make sure settings key are not duplicated
        require(
            SettingsKey.entries.size == SettingsKey.entries.map { it.key }.distinct().size
        ) { "Duplicated config key detected" }
    }
}