package presentation.feature.detail.main.view.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import core.designsystem.BookSearchTheme
import presentation.base.R as BaseR

/** 상단 바의 하트. 모양·색·설명은 목록 카드의 하트와 같다. */
@Composable
internal fun DetailFavoriteButtonView(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(if (isFavorite) BaseR.string.favorite_remove else BaseR.string.favorite_add),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailFavoriteButtonViewPreview() {
    BookSearchTheme {
        DetailFavoriteButtonView(isFavorite = true, onClick = {})
    }
}
