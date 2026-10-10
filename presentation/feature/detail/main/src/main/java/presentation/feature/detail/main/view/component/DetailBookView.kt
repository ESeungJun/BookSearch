package presentation.feature.detail.main.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing
import presentation.base.R as BaseR
import presentation.feature.detail.main.data.DetailViewData

/** 책을 찾았을 때의 본문. 위에서부터 제목(가장 크게) → 정보 블록 → 책 소개다. */
@Composable
internal fun DetailBookView(book: DetailViewData, onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.large)
            .padding(bottom = Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        Text(
            text = book.title ?: stringResource(BaseR.string.book_title_none),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        DetailInfoView(book)
        DetailDescriptionView(book.description, book.isDescriptionCut, book.url, onOpenUrl)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailBookViewPreview() {
    BookSearchTheme {
        DetailBookView(
            book = DetailViewData(
                title = "코틀린 인 액션",
                authors = "드미트리 제메로프",
                publisher = "에이콘출판",
                publishedDate = "2017-10-01",
                isbn = "9788960777330",
                price = 36_000,
                salePrice = null,
                thumbnailUrl = null,
                description = null,
                isDescriptionCut = true,
                url = null,
            ),
            onOpenUrl = {},
        )
    }
}
