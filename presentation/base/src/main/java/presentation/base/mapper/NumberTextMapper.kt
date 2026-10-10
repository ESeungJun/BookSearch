package presentation.base.mapper

import java.text.NumberFormat
import java.util.Locale

/** 화면에 보일 숫자 문구(세 자리마다 쉼표). 가격·건수·권수에 같이 쓴다. */
fun Int.toNumberText(): String = NumberFormat.getNumberInstance(Locale.KOREA).format(this)
