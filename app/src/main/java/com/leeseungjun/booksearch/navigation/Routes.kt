package com.leeseungjun.booksearch.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// 백스택을 프로세스 종료 후에도 복원하려면 경로가 직렬화 가능해야 한다

@Serializable
data object SearchRoute : NavKey

@Serializable
data object FavoriteRoute : NavKey

@Serializable
data class DetailRoute(val bookId: String) : NavKey
