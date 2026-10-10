package presentation.feature.detail.main.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing
import presentation.feature.detail.main.R

/** 책 소개 블록. 요약이 문장 중간에서 끊겼으면([isCut]) 끝에 "…" 를 붙이고, 전체는 도서 페이지([url])에서 보게 한다. */
@Composable
internal fun BookDescription(description: String?, isCut: Boolean, url: String?, onOpenUrl: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(stringResource(R.string.detail_description), style = MaterialTheme.typography.titleMedium)
        if (description == null) {
            Text(
                stringResource(R.string.detail_description_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                if (isCut) stringResource(R.string.detail_description_more, description) else description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (url != null) {
            TextButton(onClick = { onOpenUrl(url) }) { Text(stringResource(R.string.detail_open_full)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDescriptionPreview() {
    BookSearchTheme {
        BookDescription(
            description = "코틀린이 무엇이며, 코틀린이 왜 자바 개발자에게 좋은 언어인지 설명한다",
            isCut = true,
            url = "https://example.com",
            onOpenUrl = {},
        )
    }
}
