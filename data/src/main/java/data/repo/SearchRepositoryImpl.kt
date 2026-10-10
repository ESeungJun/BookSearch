package data.repo

import data.source.local.IBookLocalDataSource
import data.source.remote.IBookRemoteDataSource
import domain.book.data.BookException
import domain.book.data.BookException.Reason
import domain.search.data.SearchConditionDTO
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * 원격과 로컬 중 어디서 가져올지만 정한다. 검색은 **네트워크를 먼저** 시도하고,
 * 3초를 넘기거나 실패하면 저장해 둔 결과를 대신 돌려준다(D-15·D-23).
 * 연결 상태를 미리 확인하지 않는다 — 연결이 없으면 요청이 곧바로 실패해 같은 경로로 캐시를 보게 되고,
 * 연결은 있지만 느린 경우(지하철)는 연결 상태로는 알 수 없어 어차피 시간 제한이 필요하다.
 */
class SearchRepositoryImpl @Inject constructor(
    private val remote: IBookRemoteDataSource,
    private val local: IBookLocalDataSource,
) : ISearchRepository {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): Result<SearchPageDTO> {
        // 취소(CancellationException)는 잡지 않는다 — 실패가 아니므로 캐시로 넘기지 않고 호출한 쪽에 그대로 전한다
        val error = try {
            val result = withTimeoutOrNull(TIMEOUT_MS) { remote.searchBooks(query, sort, page) }
            if (result != null) {
                local.saveSearchPage(query, sort, page, result)
                return Result.success(result)
            }
            BookException(Reason.NETWORK)
        } catch (e: BookException) {
            e
        }
        return local.getSearchPage(query, sort, page)?.let { Result.success(it) } ?: Result.failure(error)
    }

    override suspend fun getLastSearch(): SearchConditionDTO? = local.getLastSearch()

    companion object {
        private const val TIMEOUT_MS = 3_000L
    }
}
