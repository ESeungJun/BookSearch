package domain.favorite

import domain.book.data.BookDTO
import domain.favorite.data.FavoriteSort
import domain.favorite.repo.IFavoriteRepository
import domain.favorite.usecase.ObserveFavoritesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFavoritesUseCaseTest {
    private val favorites = listOf(
        book("b", title = "나 코틀린", price = 15000),
        book("a", title = "가 자바", price = 30000, author = "코틀린 작가"),
        book("c", title = "다 가격 없음", price = null),
    )
    private val useCase = ObserveFavoritesUseCase(FakeRepository(favorites))

    @Test
    fun `검색어는 제목과 저자에서 찾는다`() = runTest {
        val result = useCase("코틀린", FavoriteSort.TITLE_ASC, null).first()
        assertEquals(listOf("a", "b"), result.map { it.key })
    }

    @Test
    fun `제목 내림차순으로 정렬한다`() = runTest {
        val result = useCase("", FavoriteSort.TITLE_DESC, null).first()
        assertEquals(listOf("c", "b", "a"), result.map { it.key })
    }

    @Test
    fun `금액 범위를 고르면 범위 밖과 가격 없는 책이 빠진다`() = runTest {
        val result = useCase("", FavoriteSort.TITLE_ASC, 10000..19999).first()
        assertEquals(listOf("b"), result.map { it.key })
    }

    private fun book(key: String, title: String, price: Int?, author: String = "저자") =
        BookDTO(key, title, listOf(author), "출판사", "", price, null, null, "", "", "")

    private class FakeRepository(private val favorites: List<BookDTO>) : IFavoriteRepository {
        override fun observeFavorites(): Flow<List<BookDTO>> = flowOf(favorites)
        override suspend fun addFavorite(book: BookDTO) = Unit
        override suspend fun removeFavorite(key: String) = Unit
    }
}
