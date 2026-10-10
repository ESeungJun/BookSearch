package data.local.book.source.local

import core.database.dao.IBookDao
import data.base.toBook
import domain.base.data.BookDTO
import javax.inject.Inject

/** Room 의 suspend DAO 는 자체 스레드에서 실행되므로 여기서 디스패처를 바꾸지 않는다. */
class BookLocalDataSourceImpl @Inject constructor(
    private val bookDao: IBookDao,
) : IBookLocalDataSource {

    override suspend fun getBook(key: String): BookDTO? = bookDao.get(key)?.toBook()
}
