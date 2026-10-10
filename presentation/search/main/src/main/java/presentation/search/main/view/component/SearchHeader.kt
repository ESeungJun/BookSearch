package presentation.search.main.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.component.SearchField
import core.designsystem.theme.Spacing
import presentation.search.main.R
import presentation.search.main.data.SearchNotice

/** 스크롤하면 접히는 머리: 제목·검색창·오프라인 안내 줄. */
@Composable
internal fun SearchHeader(query: String, notice: SearchNotice?, onQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Text(stringResource(R.string.search_title), style = MaterialTheme.typography.headlineMedium)
        SearchField(query, onQueryChange, stringResource(R.string.search_placeholder))
        if (notice != null) OfflineNotice(notice)
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchHeaderPreview() {
    BookSearchTheme {
        SearchHeader(query = "코틀린", notice = SearchNotice.LocalMatch, onQueryChange = {})
    }
}
