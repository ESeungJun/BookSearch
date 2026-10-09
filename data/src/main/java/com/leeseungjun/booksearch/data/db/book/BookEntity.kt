package com.leeseungjun.booksearch.data.db.book

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leeseungjun.booksearch.domain.model.Book

/** 책 정보는 이 테이블 한 곳에만 둔다. 즐겨찾기와 검색 캐시는 [key] 로 가리킨다. */
@Entity(tableName = "book")
data class BookEntity(
    @PrimaryKey val key: String,
    val title: String,
    val authors: List<String>,
    val publisher: String,
    val publishedDate: String,
    val price: Int?,
    val salePrice: Int?,
    val thumbnailUrl: String?,
    val isbn: String,
    val description: String,
    val url: String,
)

fun BookEntity.toBook() = Book(key, title, authors, publisher, publishedDate, price, salePrice, thumbnailUrl, isbn, description, url)

fun Book.toEntity() = BookEntity(key, title, authors, publisher, publishedDate, price, salePrice, thumbnailUrl, isbn, description, url)
