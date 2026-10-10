package com.leeseungjun.booksearch.data.api.searchbook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// kotlinx.serialization 은 컴파일 시점에 직렬화 코드를 만든다. 리플렉션을 쓰지 않아
// R8 이 필드 이름을 바꿔도 @SerialName 기준으로 파싱된다(Gson 은 이 경우 keep 규칙이 필요하다).

@Serializable
data class SearchBookApi(
    val meta: MetaApi,
    val documents: List<DocumentApi>,
) {
    @Serializable
    data class MetaApi(
        @SerialName("total_count") val totalCount: Int,
        @SerialName("is_end") val isEnd: Boolean,
    )

    @Serializable
    data class DocumentApi(
        val title: String = "",
        val contents: String = "",
        val url: String = "",
        val isbn: String = "",
        val datetime: String = "",
        val authors: List<String> = emptyList(),
        val publisher: String = "",
        val price: Int = 0,
        @SerialName("sale_price") val salePrice: Int = -1,
        val thumbnail: String = "",
    )
}
