package info.skyblond.vazan.browse

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import info.skyblond.vazan.NoteDetailsActivity
import info.skyblond.vazan.UUIDText
import kotlinx.coroutines.flow.Flow
import java.util.*

@Composable
fun UUIDList(uuidPager: Flow<PagingData<UUID>>, highlight: String? = null) {
    val items = uuidPager.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp, 10.dp),
        content = {
            itemsIndexed(items) { _, uuid ->
                check(uuid != null) { "UUID is null" }
                UUIDCard(uuid, highlight)
            }
        }
    )
}

@Composable
private fun UUIDCard(uuid: UUID, highlight: String?) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(5.dp, 7.dp)
            .width(IntrinsicSize.Max)
            .clickable {
                val noteIntent = Intent(context, NoteDetailsActivity::class.java)
                noteIntent.putExtra("uuid", uuid.toString())
                noteIntent.putExtra("highlight", highlight)
                context.startActivity(noteIntent)
            }
    ) {
        UUIDText(
            uuid = uuid, modifier = Modifier
                .padding(10.dp)
        )
    }
}