package data.local.book.repo

import data.base.safeDbCall
import data.base.toBook
import data.local.book.source.local.IBookLocalDataSource
import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.book.repo.IBookRepository
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val local: IBookLocalDataSource,
) : IBookRepository {

    override suspend fun getBook(key: String): DomainResult<BookDTO?> = safeDbCall { local.getBook(key)?.toBook() }
}
