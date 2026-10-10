package presentation.feature.detail.main.view

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import core.designsystem.BookSearchTheme
import core.designsystem.component.EmptyView
import core.designsystem.component.ErrorView
import presentation.base.view.ToastEffect
import presentation.base.view.component.FavoriteIconButton
import presentation.feature.detail.main.R
import presentation.feature.detail.main.data.DetailUiState
import presentation.feature.detail.main.data.DetailUiStatus
import presentation.feature.detail.main.data.DetailViewData
import presentation.feature.detail.main.view.component.DetailBookView
import presentation.feature.detail.main.view.component.DetailSkeletonView
import presentation.feature.detail.main.vm.DetailViewModel
import presentation.base.R as BaseR

@Composable
fun DetailScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.IFactory> { it.create(bookId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ToastEffect(state.toastRes, viewModel::onToastShown)
    val context = LocalContext.current
    val noBrowserMessage = stringResource(R.string.detail_no_browser)
    DetailContent(
        state = state,
        onBack = onBack,
        onFavoriteClick = viewModel::onFavoriteClick,
        onRetry = viewModel::retry,
        onOpenUrl = { url ->
            // 기기에 브라우저가 하나도 없으면 열 수 없다. 그 사실만 알린다
            try {
                CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, noBrowserMessage, Toast.LENGTH_SHORT).show()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
                }
            },
            actions = {
                // 책을 찾았을 때만 하트가 의미가 있다
                if (state.status is DetailUiStatus.Loaded) FavoriteIconButton(state.isFavorite, onFavoriteClick)
            },
        )
        when (val status = state.status) {
            DetailUiStatus.Loading -> DetailSkeletonView()
            is DetailUiStatus.Loaded -> DetailBookView(status.book, onOpenUrl)
            DetailUiStatus.NotFound -> EmptyView(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = stringResource(R.string.detail_not_found),
                action = { FilledTonalButton(onClick = onBack) { Text(stringResource(R.string.detail_back)) } },
            )
            DetailUiStatus.Error -> ErrorView(stringResource(BaseR.string.result_failure), onRetry)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailContentPreview() {
    BookSearchTheme {
        DetailContent(
            state = DetailUiState(
                status = DetailUiStatus.Loaded(
                    DetailViewData(
                        title = "코틀린 인 액션",
                        authors = "드미트리 제메로프, 스베트라나 이사코바",
                        publisher = "에이콘출판",
                        publishedDate = "2017-10-01",
                        isbn = "9788960777330",
                        price = 36_000,
                        salePrice = 32_400,
                        thumbnailUrl = null,
                        description = "코틀린이 무엇이며, 코틀린이 왜 자바 개발자에게 좋은 언어인지 설명하고 코틀린의 기초를 다룬다",
                        isDescriptionCut = true,
                        url = "https://example.com",
                    ),
                ),
                isFavorite = true,
            ),
            onBack = {},
            onFavoriteClick = {},
            onRetry = {},
            onOpenUrl = {},
        )
    }
}
