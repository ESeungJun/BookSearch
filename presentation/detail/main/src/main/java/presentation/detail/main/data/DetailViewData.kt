package presentation.detail.main.data

import androidx.compose.runtime.Immutable

/** 상세에 그릴 값. 서버가 주지 않은 값(null·빈 문자열)은 null 이고, 그 줄은 화면이 생략한다. */
@Immutable
data class DetailViewData(
    val title: String?,
    val authors: String?, // 모든 저자를 ", " 로 잇는다
    val publisher: String?,
    val publishedDate: String?,
    val isbn: String?,
    val price: Int?, // 정상가
    val salePrice: Int?, // 할인가. 할인이 없으면 null
    val thumbnailUrl: String?,
    val description: String?, // 서버가 주는 요약(앞부분만 온다)
    val isDescriptionCut: Boolean, // 요약이 문장 중간에서 끊겼으면 true — 끝에 "…" 를 붙인다
    val url: String?, // 전체 소개가 있는 도서 페이지
)
