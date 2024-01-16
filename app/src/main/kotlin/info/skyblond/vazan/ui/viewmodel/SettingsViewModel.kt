package info.skyblond.vazan.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.room.Config
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
) : ViewModel() {
    // each config has a persistent status, thus we can update them if someone changed it
    private val configMap = ConcurrentHashMap<SettingsKey, MutableState<String>>()

    fun getConfigByKey(key: SettingsKey): MutableState<String> {
        val state = configMap.getOrPut(key) { mutableStateOf("") }
        viewModelScope.launch {
            val v = configRepo.getConfigByKey(key)?.value ?: ""
            state.value = v
        }
        return state
    }

    fun updateConfigByKey(key: SettingsKey, newValue: String) = viewModelScope.launch {
        val c = configRepo.getConfigByKey(key)?.copy(value = newValue) ?: Config(key.key, newValue)
        configRepo.insertOrUpdateConfig(c)
        getConfigByKey(key) // refresh ui
    }
}