package presentation.feature.search.main.view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing
import presentation.feature.search.main.R
import core.designsystem.R as DsR

/**
 * 목록 끝. 다음 페이지를 받는 중이면 프로그레스, 실패했으면 다시 시도.
 * 다음 페이지 실패는 이미 받은 목록을 지우지 않고 여기서만 알린다.
 */
@Composable
internal fun SearchLoadMoreView(isFailed: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isFailed) {
            Text(
                stringResource(R.string.search_load_more_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) { Text(stringResource(DsR.string.action_retry)) }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchLoadMoreViewPreview() {
    BookSearchTheme {
        SearchLoadMoreView(isFailed = true, onRetry = {})
    }
}
