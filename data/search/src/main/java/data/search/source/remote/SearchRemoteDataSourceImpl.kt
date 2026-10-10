package data.search.source.remote

import data.search.data.toBook
import data.search.service.ISearchBookService
import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import javax.inject.Inject

class SearchRemoteDataSourceImpl @Inject constructor(
    private val service: ISearchBookService,
) : ISearchRemoteDataSource {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> {
        val response = try {
            service.search(query, sort.apiValue, page, PAGE_SIZE)
        } catch (e: CancellationException) {
            // 화면을 떠나거나 새 검색으로 이전 요청이 취소된 것이다. 실패가 아니므로 그대로 전한다
            throw e
        } catch (e: HttpException) {
            // 상태 코드만 넘긴다. 401 응답 본문에 키 일부가 들어 있어 예외를 그대로 넘기지 않는다
            return DomainResult.Fail(e.code())
        } catch (e: Exception) {
            return DomainResult.Error(e)
        }
        // 목록·총 개수·끝 여부가 없으면 페이지를 만들 수 없다. 빈 목록으로 보지 않고 형식 오류로 다룬다
        val meta = response.meta
        val documents = response.documents
        if (meta?.totalCount == null || meta.isEnd == null || documents == null) {
            return DomainResult.Error(IllegalStateException("검색 응답에 meta 또는 documents 가 없다"))
        }
        return DomainResult.Success(SearchPageDTO(documents.map { it.toBook() }, meta.totalCount, meta.isEnd))
    }

    private val SearchSort.apiValue: String
        get() = when (this) {
            SearchSort.ACCURACY -> "accuracy"
            SearchSort.LATEST -> "latest"
        }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
