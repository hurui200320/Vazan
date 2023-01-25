package info.skyblond.vazan.browse

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import info.skyblond.vazan.VazanActivity
import info.skyblond.vazan.database.SearchKeywordViewModel
import info.skyblond.vazan.ui.theme.VazanTheme

class SearchNoteContentActivity : VazanActivity() {
    override val permissionExplanation: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val keyword = try {
            intent.getStringExtra("keyword")!!.also { require(it.isNotBlank()) }
        } catch (_: Throwable) {
            showToast("Invalid keyword")
            finish()
            return
        }
        val viewModel = SearchKeywordViewModel(keyword)

        setContent {
            VazanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) { UUIDList(viewModel.uuidPager, keyword) }
            }
        }
    }
}
