package info.skyblond.vazan.browse

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Modifier
import info.skyblond.vazan.NoteDetailsActivity
import info.skyblond.vazan.VazanActivity
import info.skyblond.vazan.database.NoteUUIDViewModel
import info.skyblond.vazan.ui.theme.VazanTheme
import java.util.*


class BrowseNotesActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = NoteUUIDViewModel()

        setContent {
            VazanTheme {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            val input = createSingleLineEditText()
                            AlertDialog.Builder(this)
                                .setTitle("Input UUID manually")
                                .setView(input)
                                .setCancelable(false) // prevent miss touch
                                .setPositiveButton("OK") { _, _ ->
                                    val str = input.text.toString()
                                    try {
                                        val noteIntent =
                                            Intent(this, NoteDetailsActivity::class.java)
                                        noteIntent.putExtra("uuid", UUID.fromString(str).toString())
                                        startActivity(noteIntent)
                                    } catch (_: Throwable) {
                                        showToast("Invalid UUID")
                                    }
                                }
                                .setCancelButton()
                                .create().show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search for uuid"
                            )
                        }
                    }
                ) { scaffoldPadding ->
                    Surface(
                        modifier = Modifier.padding(scaffoldPadding),
                        color = MaterialTheme.colors.background
                    ) { UUIDList(viewModel.uuidPager) }
                }
            }
        }
    }
}
