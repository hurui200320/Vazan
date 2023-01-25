package info.skyblond.vazan.browse

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import info.skyblond.vazan.NoteDetailsActivity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Composable
fun UUIDList(uuidPager: Flow<PagingData<UUID>>, highlight: String? = null) {
    val items = uuidPager.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(7.dp, 20.dp),
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
    val title = uuid.toString().uppercase()
    Card(
        modifier = Modifier
            .padding(10.dp, 12.dp)
            .width(IntrinsicSize.Max)
            .clickable {
                val noteIntent = Intent(context, NoteDetailsActivity::class.java)
                noteIntent.putExtra("uuid", uuid.toString())
                noteIntent.putExtra("highlight", highlight)
                context.startActivity(noteIntent)
            }
    ) {
        Text(
            text = title, modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(10.dp)
        )
    }
}