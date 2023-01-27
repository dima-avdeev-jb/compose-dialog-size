import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

fun main() = application {
    var dialogVisible by remember { mutableStateOf(true) }

    Window(onCloseRequest = ::exitApplication) {
        Button(onClick = {
            dialogVisible = true
        }) {
            Text("Open Dialog")
        }
        val dialogState = rememberDialogState(position = WindowPosition(0.dp, 0.dp), size = DpSize(400.dp, 400.dp))
        Dialog(
            title = "Title",
            onCloseRequest = { dialogVisible = false },
            state = dialogState,
            focusable = true,
            visible = dialogVisible,
        ) {
            Box(
                modifier = Modifier
                    .size(dialogState.size)
                    .border(1.dp, Color.Red),
                contentAlignment = Alignment.Center,
            ) {

                Column(Modifier.align(Alignment.CenterEnd)) {
                    Button({
                        dialogState.size += DpSize(10.dp, 0.dp)
                    }) { Text("+") }
                    Button({
                        dialogState.size += DpSize(-10.dp, 0.dp)
                    }) { Text("-") }
                }
                Row(Modifier.align(Alignment.BottomCenter)) {
                    Button({
                        dialogState.size += DpSize(0.dp, 10.dp)
                    }) { Text("+") }
                    Button({
                        dialogState.size += DpSize(0.dp, -10.dp)
                    }) { Text("-") }
                }
            }
        }
    }
}

private operator fun WindowPosition.plus(dpOffset: DpOffset):WindowPosition {
    return WindowPosition(x = x + dpOffset.x, y = y + dpOffset.y)
}

@Composable
fun BoxScope.BtnIcon(align: Alignment, icon: ImageVector, onClick: () -> Unit) {
    IconButton(modifier = Modifier.align(align),
        content = { Icon(icon, null) },
        onClick = {

        })
}
