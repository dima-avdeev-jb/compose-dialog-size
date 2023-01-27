import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

fun main() = application {
    var visible by remember { mutableStateOf(true) }
    Window(onCloseRequest = ::exitApplication) {
        if (visible) {
            Dialog(
                "Title",
                onCloseRequest = {
                    visible = false
                },
            ) {
                Column {
                    Text("Test")
                    Button(onClick = {
                        visible = !visible
                    }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun Dialog(
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onCloseRequest = onCloseRequest,
        state = rememberDialogState(size = DpSize(400.dp, 400.dp), position = WindowPosition(0.dp, 0.dp)),
        focusable = true,
        title = title,
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .border(1.dp, Color.Red)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
