package presentation.feature.favorite.main.view.component

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
import java.text.NumberFormat
import java.util.Locale
import presentation.feature.favorite.main.R
import presentation.feature.favorite.main.data.PriceRange
import presentation.feature.favorite.main.data.TitleSort

/** 스크롤해도 남는 고정 줄: 왼쪽 "거른 수 / 전체 수", 오른쪽 금액 필터와 제목 정렬. */
@Composable
internal fun CountFilterRow(
    shownCount: Int,
    totalCount: Int,
    priceRange: PriceRange,
    sort: TitleSort,
    onPriceRangeChange: (PriceRange) -> Unit,
    onSortChange: (TitleSort) -> Unit,
) {
    val format = NumberFormat.getNumberInstance(Locale.KOREA)
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.favorite_count, format.format(shownCount), format.format(totalCount)),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
        )
        DropdownSelector(
            selected = priceRange,
            options = PriceRange.entries,
            label = { stringResource(it.labelRes) },
            onSelect = onPriceRangeChange,
        )
        DropdownSelector(
            selected = sort,
            options = TitleSort.entries,
            label = { stringResource(it.labelRes) },
            onSelect = onSortChange,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CountFilterRowPreview() {
    BookSearchTheme {
        CountFilterRow(
            shownCount = 3,
            totalCount = 12,
            priceRange = PriceRange.FROM_10K_TO_20K,
            sort = TitleSort.ASCENDING,
            onPriceRangeChange = {},
            onSortChange = {},
        )
    }
}
