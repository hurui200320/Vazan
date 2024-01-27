package info.skyblond.vazan.ui.composable

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.model.JimMeta
import info.skyblond.vazan.ui.activity.EntryDetailActivity
import info.skyblond.vazan.ui.intent

@Composable
fun EntryTag(
    tag: JimMeta,
    modifier: Modifier = Modifier,
    cardContainerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    cardContentColor: Color = contentColorFor(cardContainerColor),
) {
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
            contentColor = cardContentColor
        ),
        modifier = modifier.padding(5.dp, 3.dp)
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
                text = tag.name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(5.dp, 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryTagFlow(metadataList: List<JimMeta>, onClick: ((JimMeta) -> Unit)? = null) {
    FlowRow(
        modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
    ) {
        metadataList.forEach { tag ->
            val modifier = if (onClick != null) {
                Modifier.clickable { onClick(tag) }
            } else Modifier
            EntryTag(tag = tag, modifier = modifier)
        }
    }
}

@Composable
private fun String.handleKeywords(
    keywords: List<String>,
    keywordStyle: @Composable (String) -> SpanStyle
): AnnotatedString {
    var sIndex = 0
    // startIndex, keyword index
    val result = mutableListOf<Pair<Int, Int>>()
    while (sIndex < this.length) {
        val (keywordIndex, i) = keywords.mapIndexed { index, k ->
            index to this.indexOf(k, startIndex = sIndex, ignoreCase = true)
        }.filter { it.second != -1 }.minByOrNull { it.second } ?: (0 to -1)
        // check if we really found a match
        if (i == -1) break
        // now we get the first keyword that matches
        sIndex = i + keywords[keywordIndex].length
        result.add(i to keywordIndex)
    }
    return buildAnnotatedString {
        append(this@handleKeywords)
        while (result.isNotEmpty()) {
            val (i, kIndex) = result.removeFirst()
            addStyle(
                keywordStyle(keywords[kIndex]),
                start = i,
                end = i + keywords[kIndex].length
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryCard(
    entry: JimEntry,
    keywords: List<String> = emptyList(),
    keywordStyle: @Composable (String) -> SpanStyle = { SpanStyle() }
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        contentAlignment = Alignment.Center
    ) {
        Card(
            onClick = {
                context.startActivity(
                    context.intent(EntryDetailActivity::class).apply {
                        putExtra(EntryDetailActivity.INTENT_STRING_EXTRA_ENTRY_ID, entry.entryId)
                    }
                )
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "${entry.type} ${entry.entryId.handleKeywords(keywords, keywordStyle)}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 2.dp)
            )
            Text(
                text = entry.name.handleKeywords(keywords, keywordStyle),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(10.dp, 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            entry.note.lines().take(2).forEach {
                Text(
                    text = it.handleKeywords(keywords, keywordStyle),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp, 0.dp, 10.dp, 0.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            EntryTagFlow(entry.metaList.filter { it.type == "TAG" })

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