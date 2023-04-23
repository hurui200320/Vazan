package info.skyblond.vazan.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.room.Config
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.model.LibraryBrief
import info.skyblond.vazan.domain.model.LibraryField
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val mementoRepository: MementoRepository
) : ViewModel() {
    private val tag = "SettingsViewModel"

    lateinit var showToast: (String) -> Unit

    // each config has a persistent status, thus we can update them if someone changed it
    private val configMap = ConcurrentHashMap<String, MutableState<String>>()

    fun getConfigByKey(key: String): MutableState<String> {
        val state = configMap.getOrPut(key) { mutableStateOf("") }
        viewModelScope.launch {
            val v = configRepo.getConfigByKey(key)?.value ?: ""
            state.value = v
        }
        return state
    }

    fun updateConfigByKey(key: String, newValue: String) = viewModelScope.launch {
        val c = configRepo.getConfigByKey(key)?.copy(value = newValue) ?: Config(key, newValue)
        configRepo.insertOrUpdateConfig(c)
        getConfigByKey(key) // refresh ui
    }

    // Note: here we assume the api key is not changed. If changed, it still return the old libraries
    // unless user exit current activity and enter again.
    private val libraryList = mutableStateListOf<LibraryBrief>()

    fun getLibraryList(): SnapshotStateList<LibraryBrief> {
        // a simple cache, so we don't call api 3 times for selecting lib id
        if (libraryList.isNotEmpty()) return libraryList
        viewModelScope.launch {
            try {
                mementoRepository.listLibraries().let {
                    libraryList.clear()
                    libraryList.addAll(it)
                }
            } catch (e: IOException) {
                Log.e(tag, "Error when fetching libraries", e)
                showToast("Failed to list libraries: ${e.message}")
                return@launch
            } catch (e: HttpException) {
                Log.e(tag, "Error when fetching libraries", e)
                showToast("Failed to list libraries: ${e.message}")
                return@launch
            }
        }
        return libraryList
    }

    private val fieldsMap = ConcurrentHashMap<SettingsKey, SnapshotStateList<LibraryField>>()

    fun getLibraryFields(settingsKey: SettingsKey): SnapshotStateList<LibraryField> {
        val list = fieldsMap.getOrPut(settingsKey) { mutableStateListOf() }
        // a simple cache, so we don't call api multiple times to select field
        if (list.isNotEmpty()) return list
        viewModelScope.launch {
            try {
                val libId = configRepo.getConfigByKey(settingsKey.key)?.value ?: ""
                mementoRepository.getLibraryFields(libId).let {
                    list.clear()
                    list.addAll(it)
                }
            } catch (e: IOException) {
                Log.e(tag, "Error when fetching library fields", e)
                showToast("Failed to list library fields: ${e.message}")
                return@launch
            } catch (e: HttpException) {
                Log.e(tag, "Error when fetching library fields", e)
                showToast("Failed to list library fields: ${e.message}")
                return@launch
            }
        }
        return list
    }
}