package data.source.remote

import data.data.toBook
import data.service.ISearchBookService
import domain.book.data.BookException
import domain.book.data.BookException.Reason
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BookRemoteDataSourceImpl @Inject constructor(
    private val service: ISearchBookService,
) : IBookRemoteDataSource {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): SearchPageDTO {
        val response = try {
            service.search(query, sort.apiValue, page, PAGE_SIZE)
        } catch (e: CancellationException) {
            // 화면을 떠나거나 새 검색으로 이전 요청이 취소된 것이다. 실패가 아니므로 그대로 전한다
            throw e
        } catch (e: Exception) {
            throw e.toBookException()
        }
        return SearchPageDTO(response.documents.map { it.toBook() }, response.meta.totalCount, response.meta.isEnd)
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
