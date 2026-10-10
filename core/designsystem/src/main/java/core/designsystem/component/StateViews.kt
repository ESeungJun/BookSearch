package core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import core.designsystem.R
import core.designsystem.theme.Spacing

private val StateIconSize = 48.dp

/** 보여 줄 내용이 없을 때(첫 진입·빈 결과) 화면 가운데에 두는 안내. [action] 은 빈 상태를 벗어나는 버튼이다. */
@Composable
fun EmptyView(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    CenteredMessage(icon = icon, title = title, modifier = modifier, description = description, action = action)
}

/** 화면 단위 실패. 원인별 문구는 부르는 쪽이 정한다. */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(icon = Icons.Outlined.ErrorOutline, title = message, modifier = modifier) {
        FilledTonalButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun CenteredMessage(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(StateIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        action?.invoke()
    }
}

/**
 * 목록 끝. 다음 페이지를 받는 중이면 프로그레스, 실패했으면 다시 시도.
 * 다음 페이지 실패는 이미 받은 목록을 지우지 않고 여기서만 알린다.
 */
@Composable
fun LoadMoreFooter(
    isFailed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isFailed) {
            Text(
                stringResource(R.string.load_more_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        } else {
            CircularProgressIndicator()
        }
    }
}
