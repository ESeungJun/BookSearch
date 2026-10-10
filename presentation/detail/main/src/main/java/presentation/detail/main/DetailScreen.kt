package presentation.detail.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// 1단계 골격: 전달받은 id 만 보여 준다. 4단계(상세)에서 교체한다.
@Composable
fun DetailScreen(bookId: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("상세: $bookId")
    }
}
