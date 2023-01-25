package info.skyblond.vazan

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import info.skyblond.vazan.browse.BrowseNotesActivity
import info.skyblond.vazan.browse.SearchNoteContentActivity
import info.skyblond.vazan.database.VazanDatabase
import info.skyblond.vazan.scanner.ScannerActivity
import info.skyblond.vazan.ui.theme.VazanTheme
import java.util.*

class MainActivity : VazanActivity() {

    private val callScannerForResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uuid = result.data!!.getBarcodes()
                    .firstNotNullOfOrNull {
                        try {
                            UUID.fromString(String(it.second, Charsets.ISO_8859_1))
                        } catch (_: Throwable) {
                            null
                        }
                    }
                if (uuid == null) {
                    showToast("Invalid UUID")
                } else {
                    val noteIntent = Intent(this, NoteDetailsActivity::class.java)
                    noteIntent.putExtra("uuid", uuid.toString())
                    startActivity(noteIntent)
                }
            }
        }

    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VazanDatabase.init(this)

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

                        Button(
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        PrintLabelActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(text = "Printer")
                        }

                        Button(
                            onClick = {
                                callScannerForResultLauncher.launch(
                                    Intent(
                                        this@MainActivity,
                                        ScannerActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(text = "Scanner")
                        }

                        Button(
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        BrowseNotesActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(text = "Browse")
                        }

                        Button(
                            onClick = {
                                val input = createSingleLineEditText()
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle("Input search keyword")
                                    .setView(input)
                                    .setCancelable(false) // prevent miss touch
                                    .setPositiveButton("OK") { _, _ ->
                                        val noteIntent =
                                            Intent(
                                                this@MainActivity,
                                                SearchNoteContentActivity::class.java
                                            )
                                        noteIntent.putExtra("keyword", input.text.toString())
                                        startActivity(noteIntent)
                                    }
                                    .setCancelButton()
                                    .create().show()
                            }
                        ) {
                            Text(text = "Search")
                        }

                        Button(
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        BackupActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(text = "Backup")
                        }
                    }
                }
            }
        }
    }
}
