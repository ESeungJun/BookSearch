package presentation.search.main.view.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.component.DropdownSelector
import core.designsystem.theme.Spacing
import domain.search.data.SearchSort
import java.text.NumberFormat
import java.util.Locale
import presentation.search.main.R

/** 스크롤해도 남는 고정 줄: 왼쪽 총 개수, 오른쪽 정렬. */
@Composable
internal fun CountSortRow(totalCount: Int, sort: SearchSort, onSortChange: (SearchSort) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.search_total_count, NumberFormat.getNumberInstance(Locale.KOREA).format(totalCount)),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
        )
        DropdownSelector(
            selected = sort,
            options = SearchSort.entries,
            label = { stringResource(it.labelRes()) },
            onSelect = onSortChange,
        )
    }
}

@StringRes
private fun SearchSort.labelRes(): Int = when (this) {
    SearchSort.ACCURACY -> R.string.sort_accuracy
    SearchSort.LATEST -> R.string.sort_latest
}

@Preview(showBackground = true)
@Composable
private fun CountSortRowPreview() {
    BookSearchTheme {
        CountSortRow(totalCount = 1_234, sort = SearchSort.ACCURACY, onSortChange = {})
    }
}
