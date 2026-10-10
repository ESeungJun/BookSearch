package core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
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

/** [CollapsingHeader] 가 얼마나 접혔는지. 화면이 들고 있다가 목록을 코드로 맨 위로 보낼 때 [expand] 를 부른다. */
@Stable
class CollapsingHeaderState(initialOffset: Float = 0f) {
    // 0(다 펼침) ~ -collapsibleHeight(다 접힘)
    internal var offset by mutableFloatStateOf(initialOffset)

    /** 머리를 다 펼친다. 코드로 하는 스크롤(animateScrollToItem)은 nested scroll 을 거치지 않아 머리가 따라오지 않는다. */
    fun expand() {
        offset = 0f
    }

    companion object {
        val Saver: Saver<CollapsingHeaderState, Float> = Saver(save = { it.offset }, restore = { CollapsingHeaderState(it) })
    }
}

@Composable
fun rememberCollapsingHeaderState(): CollapsingHeaderState =
    rememberSaveable(saver = CollapsingHeaderState.Saver) { CollapsingHeaderState() }

/**
 * 목록을 올리면 [collapsible] 이 위로 밀려 사라지고 [pinned] 만 남는 머리.
 * 손가락을 움직이는 방향을 머리가 먼저 받는다. 올리면 접히고, 목록 중간에서라도 내리면 바로 펼쳐진다.
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
    state: CollapsingHeaderState = rememberCollapsingHeaderState(),
    content: @Composable () -> Unit,
) {
    var collapsibleHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(collapseEnabled) {
        if (!collapseEnabled) state.expand()
    }
    val connection = remember(state, collapseEnabled) {
        object : NestedScrollConnection {
            // 목록보다 먼저 머리가 움직임을 쓴다. 머리가 다 접히거나 다 펼쳐진 뒤 남은 만큼만 목록이 움직인다
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!collapseEnabled) return Offset.Zero
                val next = (state.offset + available.y).coerceIn(-collapsibleHeight.toFloat(), 0f)
                val consumed = next - state.offset
                state.offset = next
                return Offset(0f, consumed)
            }
        }
    }

    Column(modifier.nestedScroll(connection)) {
        Box(
            Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    collapsibleHeight = placeable.height
                    val visibleHeight = (placeable.height + state.offset.roundToInt()).coerceIn(0, placeable.height)
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
