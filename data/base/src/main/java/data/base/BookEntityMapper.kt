package data.base

import core.database.entity.BookEntity
import domain.base.data.BookDTO

fun BookEntity.toBook() = BookDTO(key, title, authors, publisher, publishedDate, price, salePrice, thumbnailUrl, isbn, description, url)

fun BookDTO.toEntity() = BookEntity(key, title, authors, publisher, publishedDate, price, salePrice, thumbnailUrl, isbn, description, url)
