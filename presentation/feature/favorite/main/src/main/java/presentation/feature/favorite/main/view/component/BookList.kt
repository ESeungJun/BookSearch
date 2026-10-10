package presentation.feature.favorite.main.view.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.component.ScrollToTopButton
import core.designsystem.theme.Spacing
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import presentation.base.data.BookViewData
import presentation.base.view.component.BookCard
import presentation.feature.favorite.main.data.FavoriteUiStatus
import presentation.feature.favorite.main.data.FavoriteUiState

private const val SCROLL_TO_TOP_MIN_INDEX = 5 // 이만큼 내려야 맨 위로 버튼이 보인다

/** 즐겨찾기 카드 목록. 하트를 빼면 그 카드가 사라지는 움직임을 보인다. */
@Composable
internal fun BookList(
    state: FavoriteUiState,
    onBookClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onScrollToTop: () -> Unit,
) {
    // 검색어·금액·정렬이 바뀌면 새 상태로 맨 위부터 보인다. 하트를 빼거나 회전할 때는 위치를 지킨다
    val listState = rememberSaveable(
        state.query.trim(),
        state.priceRange,
        state.sort,
        saver = LazyListState.Saver,
    ) { LazyListState() }
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex >= SCROLL_TO_TOP_MIN_INDEX }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(state.books, key = { it.key }) { book ->
                BookCard(
                    book = book,
                    onClick = { onBookClick(book.key) },
                    onFavoriteClick = { onFavoriteClick(book.key) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        ScrollToTopButton(
            visible = showScrollToTop,
            onClick = {
                onScrollToTop()
                scope.launch { listState.animateScrollToItem(0) }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.large),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookListPreview() {
    val book = BookViewData(
        key = "1",
        title = "코틀린 인 액션",
        publishedDate = "2017-10-01",
        authors = "드미트리 제메로프",
        otherAuthorCount = 0,
        publisher = "에이콘출판",
        price = 32_400,
        originalPrice = 36_000,
        thumbnailUrl = null,
        isFavorite = true,
    )
    BookSearchTheme {
        BookList(
            state = FavoriteUiState(
                status = FavoriteUiStatus.Results,
                books = persistentListOf(book, book.copy(key = "2", title = "이펙티브 코틀린")),
            ),
            onBookClick = {},
            onFavoriteClick = {},
            onScrollToTop = {},
        )
    }
}
