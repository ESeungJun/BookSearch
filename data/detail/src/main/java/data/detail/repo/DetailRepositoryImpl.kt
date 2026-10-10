package data.detail.repo

import data.base.dbCall
import data.detail.source.local.IDetailLocalDataSource
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.detail.repo.IDetailRepository
import javax.inject.Inject

class DetailRepositoryImpl @Inject constructor(
    private val local: IDetailLocalDataSource,
) : IDetailRepository {

    override suspend fun getBook(key: String): DomainResult<BookDTO?> = dbCall { local.getBook(key) }
}
