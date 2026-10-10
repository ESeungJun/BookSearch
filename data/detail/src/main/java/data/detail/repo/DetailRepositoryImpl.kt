package data.detail.repo

import data.base.safeDbCall
import data.detail.source.local.IDetailLocalDataSource
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.detail.repo.IDetailRepository
import javax.inject.Inject

class DetailRepositoryImpl @Inject constructor(
    private val local: IDetailLocalDataSource,
) : IDetailRepository {

    override suspend fun getBook(key: String): DomainResult<BookDTO?> = safeDbCall { local.getBook(key) }
}
