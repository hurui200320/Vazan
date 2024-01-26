package info.skyblond.vazan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.domain.repository.JimRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickScanViewModel @Inject constructor(
    private val jimRepository: JimRepository
) : ViewModel() {
    lateinit var showErr: (String) -> Unit

    fun requireNonItem(target: String, action: String,  callBack: () -> Unit) = viewModelScope.launch {
        val entry = jimRepository.view(target)
        if (entry == null) {
            showErr("Entry not found")
            return@launch
        }

        if (entry.type == "ITEM") {
            showErr("Cannot $action into ITEM")
            return@launch
        }

        callBack()
    }
}