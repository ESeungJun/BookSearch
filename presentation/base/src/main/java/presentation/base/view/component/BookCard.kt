package presentation.base.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import core.designsystem.BookSearchTheme
import core.designsystem.theme.Spacing
import presentation.base.R
import presentation.base.mapper.toNumberText
import presentation.base.data.BookViewData

private val ThumbnailWidth = 64.dp
private val ThumbnailHeight = 92.dp
private const val TITLE_MAX_LINES = 2
private const val SUBTITLE_MAX_LINES = 1
private const val SUBTITLE_SEPARATOR = " - "

/** 검색·즐겨찾기 목록이 함께 쓰는 책 카드. 위계는 제목 > 가격 > 보조 줄(날짜 - 저자 - 출판사)이다. */
@Composable
fun BookCard(
    book: BookViewData,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Thumbnail(book.thumbnailUrl)
        Column(Modifier.weight(1f)) {
            Text(
                text = book.title ?: stringResource(R.string.book_title_none),
                style = MaterialTheme.typography.titleMedium,
                maxLines = TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = subtitle(book)
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = SUBTITLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Price(book.price, book.originalPrice, Modifier.padding(top = Spacing.small))
        }
        FavoriteIconButton(book.isFavorite, onFavoriteClick)
    }
}

// 이미지가 없거나 받지 못하면 아래 책 아이콘이 그대로 보인다
@Composable
private fun Thumbnail(url: String?) {
    Box(
        modifier = Modifier
            .size(ThumbnailWidth, ThumbnailHeight)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun subtitle(book: BookViewData): String {
    val authors = book.authors?.let {
        if (book.otherAuthorCount > 0) stringResource(R.string.book_authors_more, it, book.otherAuthorCount) else it
    }
    return listOfNotNull(book.publishedDate, authors, book.publisher).joinToString(SUBTITLE_SEPARATOR)
}

@Composable
private fun Price(price: Int?, originalPrice: Int?, modifier: Modifier = Modifier) {
    if (price == null) {
        Text(
            stringResource(R.string.book_price_none),
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.book_price, price.toNumberText()),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        if (originalPrice != null) {
            Text(
                stringResource(R.string.book_price, originalPrice.toNumberText()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookCardPreview() {
    BookSearchTheme {
        BookCard(
            book = BookViewData(
                key = "1",
                title = "코틀린 인 액션",
                publishedDate = "2017-10-01",
                authors = "드미트리 제메로프, 스베트라나 이사코바",
                otherAuthorCount = 1,
                publisher = "에이콘출판",
                price = 32_400,
                originalPrice = 36_000,
                thumbnailUrl = null,
                isFavorite = true,
            ),
            onClick = {},
            onFavoriteClick = {},
        )
    }
}
