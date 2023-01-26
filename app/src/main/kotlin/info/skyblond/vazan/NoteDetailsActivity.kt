package info.skyblond.vazan

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.setPadding
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import info.skyblond.vazan.database.Note
import info.skyblond.vazan.database.NoteViewModel
import info.skyblond.vazan.database.VazanDatabase
import info.skyblond.vazan.ui.theme.MaterialColor
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
            showToast("Invalid UUID")
            finish()
            return
        }

        val highlight = intent.getStringExtra("highlight")

        val viewModel = NoteViewModel(uuid)

        setContent {
            VazanTheme {
                Scaffold(
                    topBar = { TopAppBar { UUIDText(uuid = uuid) } },
                    floatingActionButton = { NoteFAB(uuid) }
                ) { scaffoldPadding ->
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.padding(scaffoldPadding),
                        color = MaterialTheme.colors.background
                    ) {
                        val items = viewModel.notePager.collectAsLazyPagingItems()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(7.dp, 20.dp),
                            content = {
                                itemsIndexed(items) { _, note ->
                                    check(note != null) { "Note is null" }
                                    NoteCard(note, highlight)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmAlert(title: String, message: String, block: () -> Unit) {
        AlertDialog.Builder(this@NoteDetailsActivity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> block() }
            .setCancelButton("No")
            .create().also {
                it.show()
                it.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(MaterialColor.DeepOrangeA700.toArgb())
            }
    }

    @Composable
    private fun NoteFAB(uuid: UUID) {
        Column {
            FloatingActionButton(onClick = {
                showDeleteConfirmAlert(
                    title = "Delete notes",
                    message = "Do you really want to delete all notes related to this UUID?"
                ) {
                    VazanDatabase.useDatabase {
                        it.noteDao().deleteNotesByUUID(uuid)
                    }
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
                    .setCancelButton()
                    .create().show()
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new note"
                )
            }
        }
    }

    @Composable
    private fun NoteCard(note: Note, highlight: String?) {
        val title = "At ${
            Calendar.getInstance().apply {
                timeInMillis = note.createTime * 1000
            }.let {
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                    it.toInstant()
                        .atZone(ZoneId.systemDefault())
                )
            }
        }: "
        Card(
            modifier = Modifier
                .padding(10.dp, 15.dp)
                .width(IntrinsicSize.Max),
            backgroundColor = highlight?.let { if (note.content.contains(it)) MaterialColor.Brown400 else null }
                ?: Color.Unspecified
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(10.dp)
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = note.content)
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    IconButton(
                        onClick = {
                            showDeleteConfirmAlert(
                                title = "Delete note",
                                message = "Do you really want to delete this note?"
                            ) {
                                VazanDatabase.useDatabase {
                                    it.noteDao().deleteNotes(note)
                                }
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete note",
                            tint = Color(0xFFF44336)
                        )
                    }
                    Spacer(modifier = Modifier.width(IntrinsicSize.Max))
                    IconButton(
                        onClick = {
                            getSystemService(ClipboardManager::class.java).setPrimaryClip(
                                ClipData.newPlainText(
                                    note.uuid.toString().uppercase(),
                                    title + "\n" + note.content
                                )
                            )
                            showToast("Note copied", Toast.LENGTH_SHORT)
                        }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy note"
                        )
                    }
                }
            }
        }
    }
}
