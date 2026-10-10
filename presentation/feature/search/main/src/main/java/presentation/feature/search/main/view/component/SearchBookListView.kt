package presentation.feature.search.main.view.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.component.LoadMoreFooter
import core.designsystem.component.ScrollToTopButton
import core.designsystem.theme.Spacing
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import presentation.base.data.BookViewData
import presentation.base.view.component.BookCard
import presentation.feature.search.main.data.LoadMoreState
import presentation.feature.search.main.data.SearchUiStatus
import presentation.feature.search.main.data.SearchUiState

private const val SCROLL_TO_TOP_MIN_INDEX = 5 // 이만큼 내려야 맨 위로 버튼이 보인다
// 끝에서 이만큼 남았을 때 다음 페이지를 부른다. 반 페이지 앞에서 불러야 빠르게 내려도 응답(약 1초)이 끝에 닿기 전에 온다
private const val LOAD_MORE_PREFETCH = 10

/** 검색 결과 카드 목록. 끝에 가까워지면 다음 페이지를 부르고, 목록 끝에 페이징 상태를 둔다. */
@Composable
internal fun SearchBookListView(
    state: SearchUiState,
    onLoadMore: () -> Unit,
    onBookClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onScrollToTop: () -> Unit,
) {
    // 다른 검색·정렬의 결과가 오면 새 상태로 맨 위부터 보인다. 같은 검색의 새로고침·회전에서는 위치를 지킨다
    val listState = rememberSaveable(state.searchedQuery, state.sort, saver = LazyListState.Saver) { LazyListState() }
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex >= SCROLL_TO_TOP_MIN_INDEX }
    }
    // 페이지를 받을 때마다(READY 로 돌아올 때마다) 다시 확인한다. 큰 화면에서 받은 목록이 한 화면에 다 들어가도 이어 받는다
    LaunchedEffect(listState, state.loadMore) {
        if (state.loadMore != LoadMoreState.READY) return@LaunchedEffect
        snapshotFlow { listState.isNearEnd() }.first { it }
        onLoadMore()
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(state.books, key = { it.key }) { book ->
                BookCard(
                    book = book,
                    onClick = { onBookClick(book.key) },
                    onFavoriteClick = { onFavoriteClick(book.key) },
                )
            }
            if (state.loadMore != LoadMoreState.END) {
                item { LoadMoreFooter(isFailed = state.loadMore == LoadMoreState.FAILED, onRetry = onLoadMore) }
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

private fun LazyListState.isNearEnd(): Boolean {
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisible >= layoutInfo.totalItemsCount - 1 - LOAD_MORE_PREFETCH
}

@Preview(showBackground = true)
@Composable
private fun SearchBookListViewPreview() {
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
        isFavorite = false,
    )
    BookSearchTheme {
        SearchBookListView(
            state = SearchUiState(
                status = SearchUiStatus.Results,
                books = persistentListOf(book, book.copy(key = "2", isFavorite = true)),
                loadMore = LoadMoreState.FAILED,
            ),
            onLoadMore = {},
            onBookClick = {},
            onFavoriteClick = {},
            onScrollToTop = {},
        )
    }
}
