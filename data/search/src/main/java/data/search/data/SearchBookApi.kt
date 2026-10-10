package data.search.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// kotlinx.serialization 은 컴파일 시점에 직렬화 코드를 만든다. 리플렉션을 쓰지 않아
// R8 이 필드 이름을 바꿔도 @SerialName 기준으로 파싱된다(Gson 은 이 경우 keep 규칙이 필요하다).

// 모든 필드를 nullable 로 두고 기본값을 넣지 않는다. 서버가 값을 안 보내거나 필드가 없으면 null 그대로 받아
// 앱이 "값 없음"으로 판단한다 — 빈 문자열·0 같은 기본값으로 채우면 "정말 빈 값"과 "안 온 값"을 구분할 수 없다.
// 필드가 아예 없어도 파싱이 실패하지 않는 것은 Json 설정의 explicitNulls = false 덕분이다(:di:search NetworkModule).
@Serializable
data class SearchBookApi(
    val meta: MetaApi?,
    val documents: List<DocumentApi>?,
) {
    @Serializable
    data class MetaApi(
        @SerialName("total_count") val totalCount: Int?,
        @SerialName("is_end") val isEnd: Boolean?,
    )

    @Serializable
    data class DocumentApi(
        val title: String?,
        val contents: String?,
        val url: String?,
        val isbn: String?,
        val datetime: String?,
        val authors: List<String>?,
        val publisher: String?,
        val price: Int?,
        @SerialName("sale_price") val salePrice: Int?,
        val thumbnail: String?,
    )
}
