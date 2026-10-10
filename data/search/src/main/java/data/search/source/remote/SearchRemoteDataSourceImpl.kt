package data.search.source.remote

import data.base.safeApiCall
import data.search.data.toPage
import data.search.service.ISearchBookService
import domain.base.data.DomainResult
import domain.search.data.SearchPageDTO
import domain.search.data.SearchSort
import javax.inject.Inject

class SearchRemoteDataSourceImpl @Inject constructor(
    private val service: ISearchBookService,
) : ISearchRemoteDataSource {

    override suspend fun searchBooks(query: String, sort: SearchSort, page: Int): DomainResult<SearchPageDTO> =
        safeApiCall { service.search(query, sort.apiValue, page, PAGE_SIZE).toPage() }

    private val SearchSort.apiValue: String
        get() = when (this) {
            SearchSort.ACCURACY -> "accuracy"
            SearchSort.LATEST -> "latest"
        }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
