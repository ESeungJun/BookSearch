package presentation.base.view

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

/** [messageRes] 가 생기면 토스트로 한 번 보이고 [onShown] 으로 알린다(ViewModel 이 값을 지워 다시 보이지 않게). */
@Composable
fun ToastEffect(@StringRes messageRes: Int?, onShown: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(messageRes) {
        if (messageRes == null) return@LaunchedEffect
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
        onShown()
    }
}
