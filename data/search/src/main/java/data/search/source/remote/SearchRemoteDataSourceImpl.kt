package data.search.source.remote

import data.search.data.toBook
import data.search.service.ISearchBookService
import domain.book.data.BookException
import domain.book.data.BookException.Reason
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SearchRemoteDataSourceImpl @Inject constructor(
    private val service: ISearchBookService,
) : ISearchRemoteDataSource {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): SearchPageDTO {
        val response = try {
            service.search(query, sort.apiValue, page, PAGE_SIZE)
        } catch (e: CancellationException) {
            // 화면을 떠나거나 새 검색으로 이전 요청이 취소된 것이다. 실패가 아니므로 그대로 전한다
            throw e
        } catch (e: Exception) {
            throw e.toBookException()
        }
        // 목록·총 개수·끝 여부가 없으면 페이지를 만들 수 없다. 빈 목록으로 보지 않고 서버 오류로 다룬다
        val meta = response.meta
        val documents = response.documents
        if (meta?.totalCount == null || meta.isEnd == null || documents == null) throw BookException(Reason.SERVER)
        return SearchPageDTO(documents.map { it.toBook() }, meta.totalCount, meta.isEnd)
    }

    private fun Exception.toBookException(): BookException {
        val reason = when {
            this is IOException -> Reason.NETWORK
            this is HttpException && (code() == 401 || code() == 403) -> Reason.AUTH
            // 5xx·429·응답 형식 오류. 자동 재시도는 하지 않고 화면의 "다시 시도"에 맡긴다(D-47)
            else -> Reason.SERVER
        }
        // 401 응답 본문에 키 일부가 들어 있어 원인 예외를 그대로 붙이지 않는다(로그로 새지 않게)
        return BookException(reason)
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
