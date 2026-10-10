package data.base

import data.base.db.entity.BookEntity
import domain.base.data.BookDTO

fun BookEntity.toBook() = BookDTO(
    key = key,
    title = title,
    authors = authors,
    publisher = publisher,
    publishedDate = publishedDate,
    price = price,
    salePrice = salePrice,
    thumbnailUrl = thumbnailUrl,
    isbn = isbn,
    description = description,
    url = url,
)

fun BookDTO.toEntity() = BookEntity(
    key = key,
    title = title,
    authors = authors,
    publisher = publisher,
    publishedDate = publishedDate,
    price = price,
    salePrice = salePrice,
    thumbnailUrl = thumbnailUrl,
    isbn = isbn,
    description = description,
    url = url,
)
