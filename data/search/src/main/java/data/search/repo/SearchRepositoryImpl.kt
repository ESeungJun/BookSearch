package data.search.repo

import data.search.source.local.ISearchLocalDataSource
import data.search.source.remote.ISearchRemoteDataSource
import domain.base.data.DomainResult
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import kotlinx.coroutines.withTimeoutOrNull
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * 원격과 로컬 중 어디서 가져올지만 정한다. 검색은 **네트워크를 먼저** 시도하고,
 * 3초를 넘기거나 실패하면 저장해 둔 결과를 대신 돌려준다.
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
        if (result is DomainResult.Success) {
            local.saveSearchPage(query, sort, page, result.data)
            return result
        }
        // 실패·에러면 저장해 둔 결과로 대신한다. 그것도 없으면 원래 결과를 그대로 돌려준다
        return local.getSearchPage(query, sort, page)?.let { DomainResult.Success(it) } ?: result
    }

    override suspend fun getLastSearch(): SearchConditionDTO? = local.getLastSearch()

    companion object {
        private const val TIMEOUT_MS = 3_000L
    }
}
