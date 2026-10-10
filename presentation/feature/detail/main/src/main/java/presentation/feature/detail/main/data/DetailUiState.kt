package presentation.feature.detail.main.data

import androidx.compose.runtime.Immutable

@Immutable
data class DetailUiState(
    val status: DetailUiStatus = DetailUiStatus.Loading,
    val isFavorite: Boolean = false,
)

sealed interface DetailUiStatus {
    data object Loading : DetailUiStatus
    data class Loaded(val book: DetailViewData) : DetailUiStatus
    data object NotFound : DetailUiStatus // 키에 해당하는 저장된 책이 없다
    data object Error : DetailUiStatus // 원인과 관계없이 한 문구로 알린다
}
