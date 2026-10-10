package presentation.favorite.main.data

import androidx.annotation.StringRes
import presentation.favorite.main.R

/**
 * 즐겨찾기 금액 필터의 구간. 실제로 내는 가격(할인가, 없으면 정가) 기준이고 [range] 는 양 끝을 포함한다.
 * 구간을 바꾸려면 이 목록만 고친다. [ALL] 은 금액으로 거르지 않는다(가격 정보가 없는 책도 보인다).
 */
enum class PriceRange(@StringRes val labelRes: Int, val range: IntRange?) {
    ALL(R.string.price_all, null),
    UNDER_10K(R.string.price_under_10k, 0..9_999),
    FROM_10K_TO_20K(R.string.price_10k_20k, 10_000..19_999),
    FROM_20K_TO_30K(R.string.price_20k_30k, 20_000..29_999),
    OVER_30K(R.string.price_over_30k, 30_000..Int.MAX_VALUE),
}
