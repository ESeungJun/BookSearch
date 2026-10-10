package presentation.search.main

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import core.designsystem.BookSearchTheme
import core.designsystem.component.CollapsingHeader
import core.designsystem.component.EmptyView
import core.designsystem.component.ErrorView
import core.designsystem.component.LoadMoreFooter
import core.designsystem.component.ScrollToTopButton
import core.designsystem.component.SearchField
import domain.search.data.SearchSort
import java.text.NumberFormat
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import presentation.base.BookCard
import presentation.base.BookViewData

private val ScreenPadding = 16.dp
private val HeaderSpacing = 8.dp
private val NoticePadding = 8.dp
private val SkeletonThumbnailWidth = 64.dp
private val SkeletonThumbnailHeight = 92.dp
private val SkeletonLineHeight = 14.dp
private const val SKELETON_CARD_COUNT = 6
private const val SKELETON_MIN_ALPHA = 0.4f
private const val SKELETON_PULSE_MS = 800
private const val SCROLL_TO_TOP_MIN_INDEX = 5 // 이만큼 내려야 맨 위로 버튼이 보인다
private const val LOAD_MORE_PREFETCH = 5 // 끝에서 이만큼 남았을 때 다음 페이지를 부른다

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
    // 새로고침 표시는 머리까지 감싼 바깥에 둔다. 목록을 내릴 때 접힌 머리가 먼저 펼쳐지고 그다음에 당겨지게 하려는 순서다
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
    ) {
        CollapsingHeader(
            collapsible = { SearchHeader(state.query, state.notice, onQueryChange) },
            pinned = {
                if (state.status == SearchStatus.Results) CountSortRow(state.totalCount, state.sort, onSortChange)
            },
            collapseEnabled = state.status == SearchStatus.Results,
        ) {
            when (val status = state.status) {
                SearchStatus.Idle -> EmptyView(Icons.Outlined.Search, stringResource(R.string.search_idle))
                SearchStatus.Loading -> SearchSkeleton()
                SearchStatus.Empty -> EmptyView(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.search_empty, state.searchedQuery),
                    description = stringResource(R.string.search_empty_hint),
                )
                is SearchStatus.Error -> ErrorView(stringResource(status.messageRes), onRetry)
                SearchStatus.Results -> BookList(state, onLoadMore, onBookClick, onFavoriteClick)
            }
        }
    }
}

@Composable
private fun SearchHeader(query: String, notice: SearchNotice?, onQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(HeaderSpacing),
    ) {
        Text(stringResource(R.string.search_title), style = MaterialTheme.typography.headlineMedium)
        SearchField(query, onQueryChange, stringResource(R.string.search_placeholder))
        if (notice != null) OfflineNotice(notice)
    }
}

@Composable
private fun OfflineNotice(notice: SearchNotice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(NoticePadding),
        horizontalArrangement = Arrangement.spacedBy(NoticePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            when (notice) {
                is SearchNotice.Cached -> stringResource(R.string.search_cache_notice, notice.savedTime)
                SearchNotice.LocalMatch -> stringResource(R.string.search_local_notice)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CountSortRow(totalCount: Int, sort: SearchSort, onSortChange: (SearchSort) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = ScreenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.search_total_count, NumberFormat.getNumberInstance(Locale.KOREA).format(totalCount)),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
        )
        SortMenu(sort, onSortChange)
    }
}

@Composable
private fun SortMenu(sort: SearchSort, onSortChange: (SearchSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(stringResource(sort.labelRes()))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SearchSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes())) },
                    onClick = {
                        expanded = false
                        onSortChange(option)
                    },
                )
            }
        }
    }
}

@StringRes
private fun SearchSort.labelRes(): Int = when (this) {
    SearchSort.ACCURACY -> R.string.sort_accuracy
    SearchSort.LATEST -> R.string.sort_latest
}

@Composable
private fun BookList(
    state: SearchUiState,
    onLoadMore: () -> Unit,
    onBookClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
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
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier.align(Alignment.BottomEnd).padding(ScreenPadding),
        )
    }
}

private fun LazyListState.isNearEnd(): Boolean {
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisible >= layoutInfo.totalItemsCount - 1 - LOAD_MORE_PREFETCH
}

/** 첫 검색 중에 보이는 카드 골격. 카드와 같은 자리에 같은 크기로 그려 결과가 왔을 때 화면이 덜 흔들린다. */
@Composable
private fun SearchSkeleton() {
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = SKELETON_MIN_ALPHA,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SKELETON_PULSE_MS), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Column(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
        repeat(SKELETON_CARD_COUNT) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = HeaderSpacing),
                horizontalArrangement = Arrangement.spacedBy(ScreenPadding),
            ) {
                SkeletonBlock(Modifier.size(SkeletonThumbnailWidth, SkeletonThumbnailHeight))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HeaderSpacing)) {
                    SkeletonLine(fraction = 0.9f)
                    SkeletonLine(fraction = 0.6f)
                    SkeletonLine(fraction = 0.3f)
                }
            }
        }
    }
}

@Composable
private fun SkeletonLine(fraction: Float, height: Dp = SkeletonLineHeight) {
    SkeletonBlock(Modifier.fillMaxWidth(fraction).height(height))
}

@Composable
private fun SkeletonBlock(modifier: Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant))
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
                status = SearchStatus.Results,
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
