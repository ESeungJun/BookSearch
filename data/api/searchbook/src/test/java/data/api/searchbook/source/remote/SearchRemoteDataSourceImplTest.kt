package data.api.searchbook.source.remote

import data.api.searchbook.data.SearchBookApi
import data.api.searchbook.service.ISearchBookService
import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 응답을 페이지로 바꾸는지, 필수 필드가 없으면 에러가 되는지 확인한다. 오류 분류는 ApiCallTest 가 본다. */
class SearchRemoteDataSourceImplTest {
    private val service = FakeService()
    private val remote = SearchRemoteDataSourceImpl(service)

    @Test
    fun `응답을 페이지로 바꾼다`() = runTest {
        service.response = SearchBookApi(SearchBookApi.MetaApi(totalCount = 3, isEnd = true), documents = emptyList())
        assertEquals(DomainResult.Success(SearchPageDTO(emptyList(), 3, true)), search())
    }

    @Test
    fun `목록이나 총 개수가 없는 응답은 빈 결과가 아니라 에러다`() = runTest {
        service.response = SearchBookApi(meta = null, documents = null)
        assertTrue(search() is DomainResult.Error)
    }

    private suspend fun search() = remote.searchBooks("kotlin", SearchSort.ACCURACY, 1)

    private class FakeService : ISearchBookService {
        var response: SearchBookApi? = null
        override suspend fun search(query: String, sort: String, page: Int, size: Int): SearchBookApi = response!!
    }
}
