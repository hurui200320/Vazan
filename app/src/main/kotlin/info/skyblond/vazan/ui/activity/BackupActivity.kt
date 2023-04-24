package info.skyblond.vazan.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.showToast
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.BackupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class BackupActivity : VazanActivity() {
    private val viewModel: BackupViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = buildMap {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "writing backup files")
            put(Manifest.permission.READ_EXTERNAL_STORAGE, "reading backup files")
        }
    }

    private val selectExportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data ?: kotlin.run {
                    showToast("Failed to get exported file path")
                    return@registerForActivityResult
                }
                val dialog = AlertDialog.Builder(this)
                    .setMessage("Exporting...").setCancelable(false).create()
                dialog.show()
                viewModel.viewModelScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                                    viewModel.database.export(writer)
                                }
                            }
                        }?.also {
                            showToast("Exported succeed!")
                        } ?: kotlin.run { showToast("Export failed: URI unavailable") }
                    } catch (t: Throwable) {
                        showToast("Exported failed: ${t.message}")
                    }
                    dialog.dismiss()
                }
            }
        }

    private fun doExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/octet-stream"
        intent.putExtra(
            Intent.EXTRA_TITLE,
            "vazan_export_${DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())}.bak"
        )
        intent.putExtra(
            DocumentsContract.EXTRA_INITIAL_URI,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toUri()
        )
        selectExportLauncher.launch(intent)
    }

    private val selectImportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data ?: kotlin.run {
                    showToast("Failed to get imported file path")
                    return@registerForActivityResult
                }
                val dialog = AlertDialog.Builder(this)
                    .setMessage("Importing...").setCancelable(false).create()
                dialog.show()
                viewModel.viewModelScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                                    viewModel.database.import(reader)
                                }
                            }
                        }?.also {
                            showToast("Import succeed!")
                        } ?: kotlin.run { showToast("Import failed: URI unavailable") }
                    } catch (t: Throwable) {
                        showToast("Failed to import json")
                    }
                    dialog.dismiss()
                }
            }
        }

    private fun doImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra(
            DocumentsContract.EXTRA_INITIAL_URI,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toUri()
        )
        selectImportLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Button(onClick = { doExport() }) { Text(text = "Export") }
                        Spacer(modifier = Modifier.fillMaxHeight(0.05f))
                        Button(onClick = { doImport() }) { Text(text = "Import") }
                    }
                }
            }
        }
    }
}
