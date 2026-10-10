package presentation.feature.detail.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.detail.usecase.GetBookUseCase
import domain.favorite.usecase.ObserveFavoriteKeysUseCase
import domain.favorite.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.base.data.failureMessageRes
import presentation.feature.detail.main.data.DetailUiStatus
import presentation.feature.detail.main.data.DetailUiState
import presentation.feature.detail.main.mapper.toDetailViewData

/** [bookId] 는 내비게이션 항목(DetailRouter.PageData)이 주는 값이라 생성할 때 넘겨받는다. */
@HiltViewModel(assistedFactory = DetailViewModel.IFactory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val bookId: String,
    private val getBookUseCase: GetBookUseCase,
    private val observeFavoriteKeysUseCase: ObserveFavoriteKeysUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    // 하트를 누르면 이 책을 그대로 넘긴다
    private var book: BookDTO? = null

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
        observeFavoriteKeysUseCase()
            .onEach { result ->
                // 키를 못 읽으면 하트만 이전 상태로 남는다. 책 정보는 그대로 볼 수 있어 화면 상태를 바꾸지 않는다
                if (result is DomainResult.Success) _uiState.update { it.copy(isFavorite = bookId in result.data) }
            }
            .launchIn(viewModelScope)
    }

    fun retry() {
        load()
    }

    fun onFavoriteClick() {
        val book = book ?: return
        // 결과는 즐겨찾기 키 관찰로 하트에 반영된다
        viewModelScope.launch { toggleFavoriteUseCase(book, isFavorite = _uiState.value.isFavorite) }
    }

    private fun load() {
        _uiState.update { it.copy(status = DetailUiStatus.Loading) }
        viewModelScope.launch { onBook(getBookUseCase(bookId)) }
    }

    private fun onBook(result: DomainResult<BookDTO?>) {
        if (result !is DomainResult.Success) {
            _uiState.update { it.copy(status = DetailUiStatus.Error(result.failureMessageRes())) }
            return
        }
        val found = result.data
        book = found
        _uiState.update {
            it.copy(status = if (found == null) DetailUiStatus.NotFound else DetailUiStatus.Loaded(found.toDetailViewData()))
        }
    }

    @AssistedFactory
    interface IFactory {
        fun create(bookId: String): DetailViewModel
    }
}
