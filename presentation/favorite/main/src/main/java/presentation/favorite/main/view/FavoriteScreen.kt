package presentation.favorite.main.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import core.designsystem.BookSearchTheme
import core.designsystem.component.CollapsingHeader
import core.designsystem.component.EmptyView
import core.designsystem.component.ErrorView
import kotlinx.collections.immutable.persistentListOf
import presentation.base.data.BookViewData
import presentation.favorite.main.R
import presentation.favorite.main.data.FavoriteUiStatus
import presentation.favorite.main.data.FavoriteUiState
import presentation.favorite.main.data.PriceRange
import presentation.favorite.main.data.TitleSort
import presentation.favorite.main.view.component.BookList
import presentation.favorite.main.view.component.CountFilterRow
import presentation.favorite.main.view.component.FavoriteHeader
import presentation.favorite.main.vm.FavoriteViewModel

@Composable
fun FavoriteScreen(
    onBookClick: (String) -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoriteContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onPriceRangeChange = viewModel::onPriceRangeChange,
        onSortChange = viewModel::onSortChange,
        onResetFilters = viewModel::onResetFilters,
        onRetry = viewModel::retry,
        onBookClick = onBookClick,
        onFavoriteClick = viewModel::onFavoriteClick,
    )
}

@Composable
fun FavoriteContent(
    state: FavoriteUiState,
    onQueryChange: (String) -> Unit,
    onPriceRangeChange: (PriceRange) -> Unit,
    onSortChange: (TitleSort) -> Unit,
    onResetFilters: () -> Unit,
    onRetry: () -> Unit,
    onBookClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CollapsingHeader(
        collapsible = { FavoriteHeader(state.query, onQueryChange) },
        pinned = {
            // 조건에 맞는 책이 없을 때도 조건을 바꿀 수 있게 고정 줄을 둔다
            if (state.status == FavoriteUiStatus.Results || state.status == FavoriteUiStatus.NoMatch) {
                CountFilterRow(
                    shownCount = state.books.size,
                    totalCount = state.totalCount,
                    priceRange = state.priceRange,
                    sort = state.sort,
                    onPriceRangeChange = onPriceRangeChange,
                    onSortChange = onSortChange,
                )
            }
        },
        collapseEnabled = state.status == FavoriteUiStatus.Results,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
    ) {
        when (val status = state.status) {
            // 로컬 조회는 몇 프레임 안에 끝나 골격을 그리면 깜빡임만 보인다. 빈 자리로 둔다
            FavoriteUiStatus.Loading -> Unit
            FavoriteUiStatus.NoFavorites -> EmptyView(Icons.Outlined.FavoriteBorder, stringResource(R.string.favorite_none))
            FavoriteUiStatus.NoMatch -> EmptyView(
                icon = Icons.Outlined.SearchOff,
                title = stringResource(R.string.favorite_no_match),
                action = {
                    FilledTonalButton(onClick = onResetFilters) { Text(stringResource(R.string.favorite_reset_filters)) }
                },
            )
            is FavoriteUiStatus.Error -> ErrorView(stringResource(status.messageRes), onRetry)
            FavoriteUiStatus.Results -> BookList(state, onBookClick, onFavoriteClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteContentPreview() {
    val book = BookViewData(
        key = "1",
        title = "코틀린 인 액션",
        publishedDate = "2017-10-01",
        authors = "드미트리 제메로프, 스베트라나 이사코바",
        otherAuthorCount = 0,
        publisher = "에이콘출판",
        price = 32_400,
        originalPrice = 36_000,
        thumbnailUrl = null,
        isFavorite = true,
    )
    BookSearchTheme {
        FavoriteContent(
            state = FavoriteUiState(
                query = "코틀린",
                priceRange = PriceRange.OVER_30K,
                status = FavoriteUiStatus.Results,
                books = persistentListOf(book, book.copy(key = "2", title = null, price = null, originalPrice = null)),
                totalCount = 12,
            ),
            onQueryChange = {},
            onPriceRangeChange = {},
            onSortChange = {},
            onResetFilters = {},
            onRetry = {},
            onBookClick = {},
            onFavoriteClick = {},
        )
    }
}
