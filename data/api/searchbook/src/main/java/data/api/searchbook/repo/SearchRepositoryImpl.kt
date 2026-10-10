package data.api.searchbook.repo

import data.api.searchbook.data.toPageOrNull
import data.api.searchbook.source.local.ISearchLocalDataSource
import data.api.searchbook.source.remote.ISearchRemoteDataSource
import data.base.safeDbCall
import data.base.toBook
import data.base.toEntity
import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import domain.search.repo.ISearchRepository
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

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
        when (val result = fetchPage(query, sort, page)) {
            is DomainResult.Success -> {
                // 저장에 실패해도 받은 결과는 그대로 보여 준다(다음에 캐시로 볼 수 없을 뿐이다)
                val data = result.data
                safeDbCall { local.saveSearchPage(query, sort, page, data.books.map { it.toEntity() }, data.totalCount, data.isEnd) }
                return result
            }
            is DomainResult.Fail, is DomainResult.Error -> {
                val fallback = savedPage(query, sort, page) ?: return result
                return DomainResult.Success(fallback)
            }
        }
    }

    /** 서버에서 받아 페이지로 바꾼다. 시간 초과는 연결 문제와 같게 보이도록 IOException 의 한 종류로 넘긴다. */
    private suspend fun fetchPage(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> {
        val response = withTimeoutOrNull(TIMEOUT_MS) { remote.searchBooks(query, sort, page) }
            ?: return DomainResult.Error(SocketTimeoutException("${TIMEOUT_MS}ms 초과"))
        return when (response) {
            // 목록·총 개수·끝 여부가 없는 응답은 빈 결과가 아니라 에러다
            is DomainResult.Success -> response.data.toPageOrNull()?.let { DomainResult.Success(it) }
                ?: DomainResult.Error(IllegalStateException("검색 응답에 목록·총 개수·끝 여부가 없다"))
            is DomainResult.Fail -> response
            is DomainResult.Error -> response
        }
    }

    /**
     * 같은 검색의 저장 결과 → (첫 페이지만) 저장된 모든 책에서 찾은 결과 순으로 찾는다. 기기 저장소를 읽지 못하면 없는 것과 같게 본다.
     * 다음 페이지는 저장된 책에서 이어 찾을 수 없어 null 이다(화면은 목록 끝에서 다시 시도).
     */
    private suspend fun savedPage(query: String, sort: SearchSort, page: Int): SearchPageDTO? {
        when (val cached = safeDbCall { cachedPage(query, sort, page) }) {
            is DomainResult.Success -> cached.data?.let { return it }
            is DomainResult.Fail, is DomainResult.Error -> Unit
        }
        if (page != FIRST_PAGE) return null
        return when (val matched = safeDbCall { local.findSavedBooks(query, sort) }) {
            // 저장된 책에서 찾은 결과는 다음 페이지가 없다. 총 개수는 찾은 수다
            is DomainResult.Success -> matched.data.takeIf { it.isNotEmpty() }?.let { books ->
                SearchPageDTO(books.map { it.toBook() }, totalCount = books.size, isEnd = true, isLocalMatch = true)
            }
            is DomainResult.Fail, is DomainResult.Error -> null
        }
    }

    private suspend fun cachedPage(query: String, sort: SearchSort, page: Int): SearchPageDTO? {
        val head = local.getCachedRows(query, sort, page).firstOrNull() ?: return null
        val books = local.getCachedBooks(query, sort, page).map { it.toBook() }
        return SearchPageDTO(books, head.totalCount, head.isEnd, cachedAt = head.savedAt)
    }

    companion object {
        private const val TIMEOUT_MS = 3_000L
        private const val FIRST_PAGE = 1
    }
}
