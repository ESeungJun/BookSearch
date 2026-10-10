package core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/**
 * 목록을 올리면 [collapsible] 이 위로 밀려 사라지고 [pinned] 만 남는 머리.
 * 목록을 올릴 때는 머리가 먼저 접히고, 내릴 때는 목록이 맨 위에 닿은 뒤에야 펼쳐진다
 * (그래서 목록 중간에서 살짝 내려도 머리가 끼어들지 않는다).
 *
 * [collapseEnabled] 가 false 면 펼친 채로 고정한다. 목록이 아닌 상태(빈 결과·오류)는 스크롤이 없어서,
 * 접힌 채 그 상태가 되면 다시 펼칠 방법이 없기 때문이다.
 */
@Composable
fun CollapsingHeader(
    collapsible: @Composable () -> Unit,
    pinned: @Composable () -> Unit,
    collapseEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // offset 은 0(다 펼침) ~ -collapsibleHeight(다 접힘)
    var offset by rememberSaveable { mutableFloatStateOf(0f) }
    var collapsibleHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(collapseEnabled) {
        if (!collapseEnabled) offset = 0f
    }
    val connection = remember(collapseEnabled) {
        object : NestedScrollConnection {
            fun consume(delta: Float): Offset {
                if (!collapseEnabled) return Offset.Zero
                val next = (offset + delta).coerceIn(-collapsibleHeight.toFloat(), 0f)
                val consumed = next - offset
                offset = next
                return Offset(0f, consumed)
            }

            // 위로 올림(delta < 0): 목록보다 먼저 머리를 접는다
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (available.y < 0f) consume(available.y) else Offset.Zero

            // 아래로 내림(delta > 0): 목록이 다 쓰고 남은 만큼만 머리를 펼친다
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (available.y > 0f) consume(available.y) else Offset.Zero
        }
    }

    Column(modifier.nestedScroll(connection)) {
        Box(
            Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    collapsibleHeight = placeable.height
                    val visibleHeight = (placeable.height + offset.roundToInt()).coerceIn(0, placeable.height)
                    layout(placeable.width, visibleHeight) {
                        placeable.place(0, visibleHeight - placeable.height)
                    }
                },
        ) {
            collapsible()
        }
        pinned()
        Box(Modifier.weight(1f)) { content() }
    }
}
