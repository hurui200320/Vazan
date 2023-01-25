package info.skyblond.vazan

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import info.skyblond.vazan.database.VazanDatabase
import info.skyblond.vazan.ui.theme.VazanTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class BackupActivity : VazanActivity() {

    override val permissionExplanation: Map<String, String> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to "writing backup files",
        Manifest.permission.READ_EXTERNAL_STORAGE to "reading backup files",
    )

    private val selectExportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data!!.data
                if (uri != null) {
                    val outputStream = contentResolver.openOutputStream(uri)!!
                    val writer = outputStream.bufferedWriter(Charsets.UTF_8)
                    val jsonWriter = JsonWriter(writer)
                    val dialog = AlertDialog.Builder(this)
                        .setMessage("Exporting json...")
                        .setCancelable(false)
                        .create()
                    dialog.show()
                    thread {
                        try {
                            VazanDatabase.dumpJson(jsonWriter)
                            runOnUiThread { showToast("Exported succeed!") }
                        } catch (t: Throwable) {
                            runOnUiThread { showToast("Exported failed") }
                            Log.e(this::class.java.canonicalName, t.message ?: "???")
                        } finally {
                            dialog.dismiss()
                            jsonWriter.close()
                            writer.close()
                            outputStream.close()
                        }
                    }
                } else {
                    showToast("Failed to get exported file path")
                }
            }
        }

    private fun doExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/json"
        intent.putExtra(
            Intent.EXTRA_TITLE,
            "vazan_export_${DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())}.json"
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
                val uri = result.data!!.data
                if (uri != null) {
                    Log.i("isv.Type", contentResolver.getType(uri) ?: "")
                    val inputStream = contentResolver.openInputStream(uri)!!
                    val reader = inputStream.bufferedReader(Charsets.UTF_8)
                    val jsonReader = JsonReader(reader)
                    val dialog = AlertDialog.Builder(this)
                        .setMessage("Importing json...")
                        .setCancelable(false)
                        .create()
                    dialog.show()
                    thread {
                        try {
                            VazanDatabase.fromJson(jsonReader)
                            runOnUiThread { showToast("Import succeed!")}
                        } catch (t: Throwable) {
                            runOnUiThread { showToast("Failed to import json")}
                            Log.e(this::class.java.canonicalName, t.message ?: "???")
                        } finally {
                            dialog.dismiss()
                            jsonReader.close()
                            reader.close()
                            inputStream.close()
                        }
                    }
                } else {
                    showToast("Failed to get imported file path")
                }
            }
        }

    private fun doImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("application/json", "application/octet-stream")
        )
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
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Button(onClick = { doExport() }) { Text(text = "Export") }

                        Button(onClick = { doImport() }) { Text(text = "Import") }
                    }
                }
            }
        }
    }
}
