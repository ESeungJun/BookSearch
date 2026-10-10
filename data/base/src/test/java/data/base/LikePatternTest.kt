package data.base

import org.junit.Assert.assertEquals
import org.junit.Test

/** 사용자 입력의 % · _ · \ 가 LIKE 와일드카드로 동작하지 않게 이스케이프하는지 확인한다. */
class LikePatternTest {
    @Test
    fun `검색어 앞뒤에 퍼센트를 붙인다`() {
        assertEquals("%코틀린%", "코틀린".toLikePattern())
    }

    @Test
    fun `퍼센트와 밑줄은 글자 그대로 찾도록 이스케이프한다`() {
        assertEquals("%100\\%%", "100%".toLikePattern())
        assertEquals("%a\\_b%", "a_b".toLikePattern())
    }

    @Test
    fun `역슬래시는 먼저 이스케이프해 이스케이프 문자와 겹치지 않는다`() {
        // a\%b → a\\\%b : 역슬래시를 두 개로 만든 뒤 % 를 이스케이프한다
        assertEquals("%a\\\\\\%b%", "a\\%b".toLikePattern())
    }
}
