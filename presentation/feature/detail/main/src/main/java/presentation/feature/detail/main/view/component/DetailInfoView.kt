package presentation.feature.detail.main.view.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing
import java.text.NumberFormat
import java.util.Locale
import presentation.base.R as BaseR
import presentation.feature.detail.main.R
import presentation.feature.detail.main.data.DetailViewData

internal val CoverWidth = 120.dp
internal val CoverHeight = 174.dp // 책 표지 비율(약 1:1.45)

/** 정보 블록: 왼쪽 큰 표지, 오른쪽 저자·출판사·출간일·ISBN·정상가·할인가. 값이 없는 줄은 그리지 않는다. */
@Composable
internal fun DetailInfoView(book: DetailViewData) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.large)) {
        Cover(book.thumbnailUrl)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            InfoRow(R.string.detail_authors, book.authors)
            InfoRow(R.string.detail_publisher, book.publisher)
            InfoRow(R.string.detail_published_date, book.publishedDate)
            InfoRow(R.string.detail_isbn, book.isbn)
            InfoRow(R.string.detail_price, book.price?.let { formatPrice(it) })
            InfoRow(R.string.detail_sale_price, book.salePrice?.let { formatPrice(it) }, emphasized = true)
        }
    }
}

// 이미지가 없거나 받지 못하면 아래 책 아이콘이 그대로 보인다
@Composable
private fun Cover(url: String?) {
    Box(
        modifier = Modifier
            .size(CoverWidth, CoverHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        if (url != null) {
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun InfoRow(@StringRes label: Int, value: String?, emphasized: Boolean = false) {
    if (value == null) return
    Column {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (emphasized) FontWeight.Bold else null,
            color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun formatPrice(value: Int): String =
    stringResource(BaseR.string.book_price, NumberFormat.getNumberInstance(Locale.KOREA).format(value))

@Preview(showBackground = true)
@Composable
private fun DetailInfoViewPreview() {
    BookSearchTheme {
        DetailInfoView(
            DetailViewData(
                title = "코틀린 인 액션",
                authors = "드미트리 제메로프, 스베트라나 이사코바",
                publisher = "에이콘출판",
                publishedDate = "2017-10-01",
                isbn = null,
                price = 36_000,
                salePrice = 32_400,
                thumbnailUrl = null,
                description = null,
                isDescriptionCut = true,
                url = null,
            ),
        )
    }
}
