package info.skyblond.vazan.ui.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import info.skyblond.vazan.ui.composable.EntryCard
import info.skyblond.vazan.ui.composable.OneLineText
import info.skyblond.vazan.ui.theme.VazanTheme
import info.skyblond.vazan.ui.viewmodel.KeywordSearchViewModel

@AndroidEntryPoint
class KeywordSearchActivity : VazanActivity() {
    private val viewModel: KeywordSearchViewModel by viewModels()

    override val permissionExplanation: Map<String, String> = emptyMap()

    @Composable
    private fun RowScope.TitleIcon(
        icon: ImageVector,
        iconDescription: String,
        iconOnClick: () -> Unit
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier
                .size(30.dp)
                .aspectRatio(1f)
                .clickable { iconOnClick() }
        )
    }

    @Composable
    private fun Title(
        title: String,
        icons: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OneLineText(
                text = title,
                initialFontSize = 25.sp
            )
            icons()
        }
    }

    @Composable
    private fun Keyword(
        modifier: Modifier = Modifier, index: Int
    ) {
        var showAlertDialog by rememberSaveable { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = modifier
                    .clickable { showAlertDialog = true }
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Keyword#${index + 1}",
                    modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.keywordsList[index],
                    modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 5.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 22.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete keyword ${index + 1}",
                modifier = Modifier
                    .size(60.dp)
                    .aspectRatio(1f)
                    .clickable { viewModel.keywordsList.removeAt(index) }
                    .padding(5.dp, 0.dp, 20.dp, 0.dp)
            )
        }



        if (showAlertDialog) {
            var textFieldStatus by rememberSaveable { mutableStateOf(viewModel.keywordsList[index]) }
            AlertDialog(
                modifier = Modifier.imePadding(),
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    decorFitsSystemWindows = true
                ),
                onDismissRequest = { showAlertDialog = false },
                title = {
                    Text(text = "Change keyword#${index + 1}")
                },
                text = {
                    TextField(
                        value = textFieldStatus,
                        onValueChange = { textFieldStatus = it.lowercase() },
                        singleLine = true,
                        isError = textFieldStatus.isBlank(),
                    )
                },
                confirmButton = {
                    Button(
                        enabled = textFieldStatus.isNotBlank(),
                        onClick = {
                            showAlertDialog = false
                            viewModel.keywordsList[index] = textFieldStatus.trim()
                        }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showAlertDialog = false
                        }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @Composable
    private fun ColumnScope.ShowKeywords() {
        // title
        Title(title = "Keywords") {
            TitleIcon(icon = Icons.Default.Add, iconDescription = "Add keywords") {
                viewModel.keywordsList.add("")
            }
            TitleIcon(icon = Icons.Default.PlayArrow, iconDescription = "Add keywords") {
                viewModel.search()
            }
        }
        // Keyword list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            itemsIndexed(
                viewModel.keywordsList,
                key = { index, _ -> index }
            ) { index, _ ->
                Divider()
                Keyword(index = index)
            }
            item { Divider() }
        }
    }

    @Composable
    private fun ColumnScope.ShowResults() {
        // title
        Title(title = "Search Result") {
            TitleIcon(
                icon = Icons.Default.Close,
                iconDescription = "Close result"
            ) {
                // clear the result to release RAM
                viewModel.resultList.clear()
                viewModel.showResult = false
            }
        }
        // Result list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            items(
                viewModel.resultList.take(1000),
                key = { it.entryId }
            ) { entry ->
                EntryCard(
                    entry = entry,
                    keywords = viewModel.keywordsList.filter { it.isNotBlank() }
                ) {
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        fontSynthesis = FontSynthesis.All,
                        background = Color.Yellow
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will intercept the back press when we're showing the result
        // so we can return to the keyword list view, instead of close the activity
        val callback = object : OnBackPressedCallback(viewModel.showResult) {
            override fun handleOnBackPressed() {
                viewModel.resultList.clear()
                viewModel.showResult = false
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            VazanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // update the callback, only enable when we showing the result
                        LaunchedEffect(key1 = viewModel.showResult) {
                            callback.isEnabled = viewModel.showResult
                        }

                        if (viewModel.showResult) {
                            ShowResults()
                        } else {
                            ShowKeywords()
                        }
                    }
                    if (viewModel.loading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.fillMaxHeight(0.5f))
                            Text(
                                text = "Loading...",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (viewModel.showResult)
            viewModel.search()
    }
}
