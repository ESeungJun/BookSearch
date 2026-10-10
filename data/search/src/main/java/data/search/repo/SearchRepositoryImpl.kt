package data.search.repo

import data.base.safeDbCall
import data.search.source.local.ISearchLocalDataSource
import data.search.source.remote.ISearchRemoteDataSource
import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import kotlinx.coroutines.withTimeoutOrNull
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * 원격과 로컬 중 어디서 가져올지만 정한다. 검색은 **네트워크를 먼저** 시도하고,
 * 3초를 넘기거나 실패하면 저장해 둔 결과를 대신 돌려준다(같은 검색의 저장 결과, 없으면 저장된 모든 책에서 찾은 결과).
 * 연결 상태를 미리 확인하지 않는다 — 연결이 없으면 요청이 곧바로 실패해 같은 경로로 캐시를 보게 되고,
 * 연결은 있지만 느린 경우(지하철)는 연결 상태로는 알 수 없어 어차피 시간 제한이 필요하다.
 */
class SearchRepositoryImpl @Inject constructor(
    private val remote: ISearchRemoteDataSource,
    private val local: ISearchLocalDataSource,
) : ISearchRepository {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> {
        // 시간 초과는 연결 문제와 같게 보이도록 IOException 의 한 종류로 넘긴다
        val result = withTimeoutOrNull(TIMEOUT_MS) { remote.searchBooks(query, sort, page) }
            ?: DomainResult.Error(SocketTimeoutException("${TIMEOUT_MS}ms 초과"))
        val failure: DomainResult<Nothing> = when (result) {
            is DomainResult.Success -> {
                // 저장에 실패해도 받은 결과는 그대로 보여 준다(다음에 캐시로 볼 수 없을 뿐이다)
                safeDbCall { local.saveSearchPage(query, sort, page, result.data) }
                return result
            }
            is DomainResult.Fail -> result
            is DomainResult.Error -> result
        }
        // 같은 검색의 저장 결과 → (첫 페이지만) 저장된 모든 책에서 찾은 결과 순으로 대신하고 원래 실패를 함께 넘긴다.
        // 다음 페이지는 저장된 책에서 이어 찾을 수 없어 실패를 그대로 돌려준다(화면은 목록 끝에서 다시 시도)
        val fallback = (safeDbCall { local.getSearchPage(query, sort, page) } as? DomainResult.Success)?.data
            ?: (if (page == FIRST_PAGE) (safeDbCall { local.findSavedBooks(query, sort) } as? DomainResult.Success)?.data else null)
            ?: return failure
        return DomainResult.Success(fallback.copy(failure = failure))
    }

    companion object {
        private const val TIMEOUT_MS = 3_000L
        private const val FIRST_PAGE = 1
    }
}
