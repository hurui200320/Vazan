package info.skyblond.vazan.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import info.skyblond.vazan.data.room.AppDatabase
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    val database: AppDatabase
) : ViewModel()