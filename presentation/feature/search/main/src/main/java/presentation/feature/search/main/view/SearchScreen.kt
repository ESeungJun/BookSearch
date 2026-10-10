package presentation.feature.search.main.view

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import core.designsystem.BookSearchTheme
import core.designsystem.component.CollapsingHeader
import core.designsystem.component.rememberCollapsingHeaderState
import core.designsystem.component.EmptyView
import core.designsystem.component.ErrorView
import domain.search.data.SearchSort
import kotlinx.collections.immutable.persistentListOf
import presentation.base.data.BookViewData
import presentation.feature.search.main.R
import presentation.feature.search.main.data.LoadMoreState
import presentation.feature.search.main.data.SearchNotice
import presentation.feature.search.main.data.SearchUiStatus
import presentation.feature.search.main.data.SearchUiState
import presentation.feature.search.main.view.component.BookList
import presentation.feature.search.main.view.component.CountSortRow
import presentation.feature.search.main.view.component.SearchHeader
import presentation.feature.search.main.view.component.SearchSkeleton
import presentation.feature.search.main.vm.SearchViewModel

@Composable
fun SearchScreen(
    onBookClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    state.toastRes?.let { res ->
        LaunchedEffect(res) {
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }
    SearchContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
        onBookClick = onBookClick,
        onFavoriteClick = viewModel::onFavoriteClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (SearchSort) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBookClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val headerState = rememberCollapsingHeaderState()
    // 새로고침 표시는 머리까지 감싼 바깥에 둔다. 목록을 내릴 때 접힌 머리가 먼저 펼쳐지고 그다음에 당겨지게 하려는 순서다
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
    ) {
        CollapsingHeader(
            collapsible = { SearchHeader(state.query, state.notice, onQueryChange) },
            pinned = {
                if (state.status == SearchUiStatus.Results) CountSortRow(state.totalCount, state.sort, onSortChange)
            },
            collapseEnabled = state.status == SearchUiStatus.Results,
            state = headerState,
        ) {
            when (val status = state.status) {
                SearchUiStatus.Idle -> EmptyView(Icons.Outlined.Search, stringResource(R.string.search_idle))
                SearchUiStatus.Loading -> SearchSkeleton()
                SearchUiStatus.Empty -> EmptyView(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.search_empty, state.searchedQuery),
                    description = stringResource(R.string.search_empty_hint),
                )
                is SearchUiStatus.Error -> ErrorView(stringResource(status.messageRes), onRetry)
                SearchUiStatus.Results -> BookList(state, onLoadMore, onBookClick, onFavoriteClick, headerState::expand)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchContentPreview() {
    val book = BookViewData(
        key = "1",
        title = "코틀린 인 액션",
        publishedDate = "2017-10-01",
        authors = "드미트리 제메로프, 스베트라나 이사코바",
        otherAuthorCount = 1,
        publisher = "에이콘출판",
        price = 32_400,
        originalPrice = 36_000,
        thumbnailUrl = null,
        isFavorite = true,
    )
    BookSearchTheme {
        SearchContent(
            state = SearchUiState(
                query = "코틀린",
                status = SearchUiStatus.Results,
                searchedQuery = "코틀린",
                books = persistentListOf(book, book.copy(key = "2", price = null, originalPrice = null, isFavorite = false)),
                totalCount = 1_234,
                page = 1,
                loadMore = LoadMoreState.READY,
                notice = SearchNotice.Cached("2026-10-10 09:30"),
            ),
            onQueryChange = {},
            onSortChange = {},
            onRefresh = {},
            onLoadMore = {},
            onRetry = {},
            onBookClick = {},
            onFavoriteClick = {},
        )
    }
}
