package info.skyblond.vazan

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.setPadding
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import info.skyblond.vazan.database.Note
import info.skyblond.vazan.database.NoteViewModel
import info.skyblond.vazan.database.VazanDatabase
import info.skyblond.vazan.ui.theme.VazanTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class NoteDetailsActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uuid = try {
            val str = intent.getStringExtra("uuid")!!
            UUID.fromString(str)
        } catch (_: Throwable) {
            Toast.makeText(this, "Invalid UUID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val viewModel = NoteViewModel(uuid)

        setContent {
            VazanTheme {
                Scaffold(
                    topBar = { TopAppBar { Text(text = uuid.toString().uppercase()) } },
                    floatingActionButton = {
                        Column {
                            FloatingActionButton(onClick = {
                                AlertDialog.Builder(this@NoteDetailsActivity)
                                    .setTitle("Delete notes")
                                    .setMessage("Do you really want to delete al notes related to this UUID?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        VazanDatabase.useDatabase {
                                            it.noteDao().deleteNotesByUUID(uuid)
                                        }
                                    }
                                    .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                                    .create().also {
                                        it.show()
                                        it.getButton(AlertDialog.BUTTON_POSITIVE)
                                            .setTextColor(android.graphics.Color.RED)
                                    }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete this uuid"
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            FloatingActionButton(onClick = {
                                val input = EditText(this@NoteDetailsActivity)
                                    .apply {
                                        setPadding(20.dpToPx())
                                        isSingleLine = false
                                        setLines(15)
                                        maxLines = 15
                                        gravity = Gravity.START or Gravity.TOP
                                        isFocusedByDefault = true
                                    }
                                AlertDialog.Builder(this@NoteDetailsActivity)
                                    .setTitle("Take a new note")
                                    .setView(input)
                                    .setCancelable(false) // prevent miss touch
                                    .setPositiveButton("OK") { _, _ ->
                                        val str = input.text.toString()
                                        if (str.isNotBlank()) {
                                            VazanDatabase.useDatabase {
                                                it.noteDao()
                                                    .insertNotes(
                                                        Note(
                                                            uuid = uuid,
                                                            createTime = System.currentTimeMillis() / 1000,
                                                            content = str
                                                        )
                                                    )
                                            }
                                        }
                                    }
                                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                                    .create().show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add new note"
                                )
                            }
                        }
                    }
                ) { scaffoldPadding ->
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.padding(scaffoldPadding),
                        color = MaterialTheme.colors.background
                    ) {
                        val items = viewModel.notePager.collectAsLazyPagingItems()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            content = {
                                itemsIndexed(items) { _, note ->
                                    check(note != null) { "Note is null" }
                                    Card(
                                        modifier = Modifier
                                            .padding(10.dp, 15.dp)
                                            .width(IntrinsicSize.Max),
                                    ) {
                                        Column(
                                            modifier = Modifier.width(IntrinsicSize.Max).padding(10.dp)
                                        ) {
                                            Text(
                                                text = "At ${
                                                    Calendar.getInstance().apply {
                                                        timeInMillis = note.createTime * 1000
                                                    }.let {
                                                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                                                            it.toInstant().atZone(ZoneId.systemDefault())
                                                        )
                                                    }
                                                }:",
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Italic
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(text = note.content)
                                        }
                                    }
                                }
                            },
                            contentPadding = PaddingValues(7.dp, 20.dp)
                        )
                    }
                }
            }
        }
    }
}
