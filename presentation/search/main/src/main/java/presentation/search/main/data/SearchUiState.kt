package presentation.search.main.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import domain.search.data.SearchSort
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import presentation.base.data.BookViewData

@Immutable
data class SearchUiState(
    val query: String = "", // 입력창 그대로(공백 포함)
    val sort: SearchSort = SearchSort.ACCURACY,
    val status: SearchUiStatus = SearchUiStatus.Idle,
    val searchedQuery: String = "", // 지금 보이는 결과의 검색어. 다음 페이지와 빈 결과 문구에 쓴다
    val books: ImmutableList<BookViewData> = persistentListOf(),
    val totalCount: Int = 0,
    val page: Int = 0, // 받은 마지막 페이지
    val loadMore: LoadMoreState = LoadMoreState.END,
    val isRefreshing: Boolean = false,
    val notice: SearchNotice? = null, // 네트워크가 실패해 대신 보여 주는 결과일 때만
    @StringRes val toastRes: Int? = null, // 화면이 한 번 보여 준 뒤 onToastShown() 으로 지운다
)

sealed interface SearchUiStatus {
    data object Idle : SearchUiStatus // 검색어 없음(첫 진입)
    data object Loading : SearchUiStatus // 첫 페이지를 받는 중. 새로고침은 isRefreshing 으로 따로 둔다
    data object Results : SearchUiStatus
    data object Empty : SearchUiStatus
    data class Error(@StringRes val messageRes: Int) : SearchUiStatus
}

enum class LoadMoreState { READY, LOADING, FAILED, END }

/** 네트워크가 실패해 대신 보여 주는 결과의 종류. */
sealed interface SearchNotice {
    data class Cached(val savedTime: String) : SearchNotice // 같은 검색의 저장 결과(그 저장 시각)
    data object LocalMatch : SearchNotice // 기기에 저장된 책에서 검색어로 찾은 결과
}
