package presentation.detail.main.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing

private val SkeletonLineHeight = 16.dp
private val SkeletonTitleHeight = 28.dp
private const val SKELETON_INFO_LINES = 4

/** 책을 찾는 동안 보이는 골격. 정상 화면과 같은 자리에 같은 크기로 그린다. */
@Composable
internal fun DetailSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        SkeletonBlock(Modifier.fillMaxWidth(0.8f).height(SkeletonTitleHeight))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.large)) {
            SkeletonBlock(Modifier.size(CoverWidth, CoverHeight))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                repeat(SKELETON_INFO_LINES) { SkeletonBlock(Modifier.fillMaxWidth(0.7f).height(SkeletonLineHeight)) }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant))
}

@Preview(showBackground = true)
@Composable
private fun DetailSkeletonPreview() {
    BookSearchTheme {
        DetailSkeleton()
    }
}
