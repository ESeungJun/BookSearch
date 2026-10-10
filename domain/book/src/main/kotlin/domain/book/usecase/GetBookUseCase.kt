package domain.book.usecase

import domain.base.data.BookDTO
import domain.base.data.DomainResult
import domain.book.repo.IBookRepository
import javax.inject.Inject

/** 목록에서 고른 책의 상세 정보를 본다. 상세 API 가 없어 검색·즐겨찾기 때 저장해 둔 정보에서 찾는다. */
class GetBookUseCase @Inject constructor(
    private val repository: IBookRepository,
) {
    suspend operator fun invoke(key: String): DomainResult<BookDTO?> = repository.getBook(key)
}
