package domain.favorite.usecase

import domain.base.data.DomainResult
import domain.favorite.repo.IFavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * 어떤 책이 즐겨찾기인지 본다. 다른 화면에서 하트를 바꿔도 바로 반영되도록 계속 관찰한다.
 * 검색 결과에는 즐겨찾기 여부를 저장하지 않고 이 값에서 계산한다 — 두 곳에 두면 서로 어긋날 수 있다.
 */
class ObserveFavoriteKeysUseCase @Inject constructor(
    private val repository: IFavoriteRepository,
) {
    operator fun invoke(): Flow<DomainResult<Set<String>>> = repository.observeFavoriteKeys()
}
