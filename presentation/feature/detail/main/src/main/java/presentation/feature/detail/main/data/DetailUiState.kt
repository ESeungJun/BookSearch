package presentation.feature.detail.main.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class DetailUiState(
    val status: DetailUiStatus = DetailUiStatus.Loading,
    val isFavorite: Boolean = false,
    @StringRes val toastRes: Int? = null, // 화면이 한 번 보인 뒤 onToastShown() 으로 지운다
)

sealed interface DetailUiStatus {
    data object Loading : DetailUiStatus
    data class Loaded(val book: DetailViewData) : DetailUiStatus
    data object NotFound : DetailUiStatus // 키에 해당하는 저장된 책이 없다
    data object Error : DetailUiStatus // 원인과 관계없이 한 문구로 알린다
}
