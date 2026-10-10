package presentation.feature.search.main.view.component

import core.designsystem.component.SkeletonBlock
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing

private val SkeletonThumbnailWidth = 64.dp
private val SkeletonThumbnailHeight = 92.dp
private val SkeletonLineHeight = 14.dp
private const val SKELETON_CARD_COUNT = 6
private const val SKELETON_MIN_ALPHA = 0.4f
private const val SKELETON_PULSE_MS = 800

/** 첫 검색 중에 보이는 카드 골격. 카드와 비슷한 모양으로 그려 결과가 왔을 때 화면이 덜 흔들린다. */
@Composable
internal fun SearchSkeletonView() {
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = SKELETON_MIN_ALPHA,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SKELETON_PULSE_MS), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Column(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
        repeat(SKELETON_CARD_COUNT) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large, vertical = Spacing.small),
                horizontalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                SkeletonBlock(Modifier.size(SkeletonThumbnailWidth, SkeletonThumbnailHeight))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    SkeletonLine(fraction = 0.9f)
                    SkeletonLine(fraction = 0.6f)
                    SkeletonLine(fraction = 0.3f)
                }
            }
        }
    }
}

@Composable
private fun SkeletonLine(fraction: Float) {
    SkeletonBlock(Modifier.fillMaxWidth(fraction).height(SkeletonLineHeight))
}

@Preview(showBackground = true)
@Composable
private fun SearchSkeletonViewPreview() {
    BookSearchTheme {
        SearchSkeletonView()
    }
}
