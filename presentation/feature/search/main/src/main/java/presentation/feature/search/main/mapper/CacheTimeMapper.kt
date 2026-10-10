package presentation.feature.search.main.mapper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val CACHE_TIME_PATTERN = "yyyy-MM-dd HH:mm"

/** 저장 시각(epoch 밀리초)을 안내 줄에 보일 시각 문구로 바꾼다. 시간대는 기본이 기기 시간대이고, 넘기면 그 시간대로 바꾼다(기기 설정과 무관하게 확인할 때). */
internal fun Long.toCacheTimeText(zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern(CACHE_TIME_PATTERN)
        .withZone(zone)
        .format(Instant.ofEpochMilli(this))
