package data.book.repo

import data.book.source.local.IBookLocalDataSource
import domain.book.data.BookDTO
import domain.book.repo.IBookRepository
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val local: IBookLocalDataSource,
) : IBookRepository {

    override suspend fun getBook(key: String): BookDTO? = local.getBook(key)
}
