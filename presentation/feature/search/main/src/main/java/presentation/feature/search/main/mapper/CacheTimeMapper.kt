package presentation.feature.search.main.mapper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val CACHE_TIME_PATTERN = "yyyy-MM-dd HH:mm"

/** 저장 시각(epoch 밀리초)을 안내 줄에 보일 기기 시간대의 시각 문구로 바꾼다. */
fun Long.toCacheTimeText(): String =
    DateTimeFormatter.ofPattern(CACHE_TIME_PATTERN)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(this))
