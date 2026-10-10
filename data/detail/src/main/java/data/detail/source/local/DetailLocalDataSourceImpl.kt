package data.detail.source.local

import data.database.dao.IBookDao
import data.database.entity.toBook
import domain.base.data.BookDTO
import javax.inject.Inject

/** Room 의 suspend DAO 는 자체 스레드에서 실행되므로 여기서 디스패처를 바꾸지 않는다. */
class DetailLocalDataSourceImpl @Inject constructor(
    private val bookDao: IBookDao,
) : IDetailLocalDataSource {

    override suspend fun getBook(key: String): BookDTO? = bookDao.get(key)?.toBook()
}
