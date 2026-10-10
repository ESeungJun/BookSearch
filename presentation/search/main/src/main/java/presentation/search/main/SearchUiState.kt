package presentation.search.main

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import domain.search.data.SearchSort
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import presentation.base.BookViewData

@Immutable
data class SearchUiState(
    val query: String = "", // 입력창 그대로(공백 포함)
    val sort: SearchSort = SearchSort.ACCURACY,
    val status: SearchStatus = SearchStatus.Idle,
    val searchedQuery: String = "", // 지금 보이는 결과의 검색어. 다음 페이지와 빈 결과 문구에 쓴다
    val books: ImmutableList<BookViewData> = persistentListOf(),
    val totalCount: Int = 0,
    val page: Int = 0, // 받은 마지막 페이지
    val loadMore: LoadMoreState = LoadMoreState.END,
    val isRefreshing: Boolean = false,
    val cachedTime: String? = null, // 저장된 결과를 보여 줄 때만 그 저장 시각
    @StringRes val toastRes: Int? = null, // 화면이 한 번 보여 준 뒤 onToastShown() 으로 지운다
)

sealed interface SearchStatus {
    data object Idle : SearchStatus // 검색어 없음(첫 진입)
    data object Loading : SearchStatus // 첫 페이지를 받는 중. 새로고침은 isRefreshing 으로 따로 둔다
    data object Results : SearchStatus
    data object Empty : SearchStatus
    data class Error(@StringRes val messageRes: Int) : SearchStatus
}

enum class LoadMoreState { READY, LOADING, FAILED, END }
