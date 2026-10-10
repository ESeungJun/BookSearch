package presentation.feature.favorite.main.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import presentation.base.data.BookViewData
import presentation.feature.favorite.main.R

@Immutable
data class FavoriteUiState(
    val query: String = "", // 입력창 그대로(공백 포함)
    val priceRange: PriceRange = PriceRange.ALL,
    val sort: TitleSort = TitleSort.ASCENDING,
    val status: FavoriteUiStatus = FavoriteUiStatus.Loading,
    val books: ImmutableList<BookViewData> = persistentListOf(), // 조건에 맞는 책, sort 순서
    val totalCount: Int = 0, // 조건 없이 센 즐겨찾기 수
)

sealed interface FavoriteUiStatus {
    data object Loading : FavoriteUiStatus
    data object NoFavorites : FavoriteUiStatus // 저장한 책이 없다
    data object Results : FavoriteUiStatus
    data object NoMatch : FavoriteUiStatus // 저장한 책은 있지만 검색어·금액에 맞는 책이 없다
    data object Error : FavoriteUiStatus // 원인과 관계없이 한 문구로 알린다
}

/** 제목 정렬 방향. 제목이 없는 책은 어느 방향이든 맨 뒤다. */
enum class TitleSort(@StringRes val labelRes: Int) {
    ASCENDING(R.string.sort_title_ascending),
    DESCENDING(R.string.sort_title_descending),
}
