package presentation.feature.favorite.main.view.component

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
import presentation.feature.favorite.main.R

/** 스크롤하면 접히는 머리: 제목과 저장한 책 검색창. */
@Composable
internal fun FavoriteHeaderView(query: String, onQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Text(stringResource(R.string.favorite_title), style = MaterialTheme.typography.headlineMedium)
        SearchField(query, onQueryChange, stringResource(R.string.favorite_placeholder))
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteHeaderViewPreview() {
    BookSearchTheme {
        FavoriteHeaderView(query = "", onQueryChange = {})
    }
}
