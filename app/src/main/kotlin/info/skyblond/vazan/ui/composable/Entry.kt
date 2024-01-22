package info.skyblond.vazan.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.model.JimMeta
import info.skyblond.vazan.ui.activity.EntryDetailActivity
import info.skyblond.vazan.ui.intent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EntryCard(entry: JimEntry) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        contentAlignment = Alignment.Center
    ) {
        Card(
            onClick = { context.startActivity(
                context.intent(EntryDetailActivity::class).apply {
                    putExtra("entry_id", entry.entryId)
                }
            ) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "${entry.type} ${entry.entryId}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 2.dp)
            )
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(10.dp, 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            entry.note.lines().take(2).forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp, 0.dp, 10.dp, 0.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FlowRow(
                modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
            ) {
                entry.metaList.filter { it.type == "TAG" }.forEach {
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.padding(5.dp, 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Default.Sell, contentDescription = "tag",
                                modifier = Modifier
                                    .size(15.dp)
                                    .padding(5.dp, 1.dp, 0.dp, 0.dp)
                            )
                            Text(
                                text = it.name, style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(5.dp, 2.dp)
                            )
                        }
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEntryCard() {
    EntryCard(
        JimEntry(
            "I123ZXCASD2", "ITEM", "B99DCU4JS9", 5,
            "Something with a suuuuuuuuuuuper long name",
            """
                multiline but first ling is suuuuuuuuuuuper long
                111 aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                hahaha
            """.trimIndent(),
            listOf(
                JimMeta("tag1", "TAG", false, ""),
                JimMeta("tag2", "TAG", false, ""),
                JimMeta("tag3_a_little_bit_long", "TAG", false, ""),
                JimMeta("tag_not_super_long", "TAG", false, ""),
                JimMeta("tag_hmmmmm", "TAG", false, ""),
                JimMeta(
                    "tag1", "TEXT", true, """
                    multiple lines
                    haha
                    !
                """.trimIndent()
                ),
            )
        )
    )
}