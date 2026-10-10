package presentation.detail.main.data

import androidx.annotation.StringRes
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
    data class Error(@StringRes val messageRes: Int) : DetailUiStatus
}
