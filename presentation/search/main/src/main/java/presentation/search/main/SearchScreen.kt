package presentation.search.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// 1단계 골격: 탭·상세 이동만 확인한다. 3단계(검색)에서 ViewModel·UiState 와 함께 교체한다.
@Composable
fun SearchScreen(onBookClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("검색")
        Button(onClick = { onBookClick("sample") }) { Text("상세 열기") }
    }
}
