package com.leeseungjun.booksearch.data.db.favorite

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey val bookKey: String,
    val savedAt: Long,
)
