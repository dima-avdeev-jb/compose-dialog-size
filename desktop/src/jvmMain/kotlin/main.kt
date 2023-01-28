import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    var dialogVisible by remember { mutableStateOf(false) }
    var windowVisibility by remember { mutableStateOf(false) }

    Window(onCloseRequest = ::exitApplication) {
        Column {
            Button(onClick = { dialogVisible = !dialogVisible }) {
                Text(if (dialogVisible) "Close Dialog" else "Open Dialog")
            }
            Button(onClick = { windowVisibility = !windowVisibility }) {
                Text(if (windowVisibility) "Close Window" else "Open Window")
            }
        }
        val dialogState = rememberDialogState(position = WindowPosition(0.dp, 0.dp), size = DpSize.Unspecified)
        Dialog(
            title = "Title",
            onCloseRequest = { dialogVisible = false },
            state = dialogState,
            focusable = true,
            visible = dialogVisible,
        ) {
            Box(
                modifier = Modifier
                    .size(600.dp)
                    .border(1.dp, Color.Red),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.align(Alignment.Center).background(Color.Yellow)) {
                    ArrowPad {
                        dialogState.position += it
                    }
                }
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

    val windowState = rememberWindowState(position = WindowPosition(300.dp, 300.dp), size = DpSize.Unspecified)
    Window(
        onCloseRequest = { println("Window, onCloseRequest") },
        state = windowState,
        undecorated = true,
        resizable = false,
        visible = windowVisibility,
        alwaysOnTop = true,
    ) {
        // Windows will fit by content size, because of DpSize.Unspecified
        Box(Modifier.size(300.dp).background(Color.Yellow)) {
            ArrowPad {
                windowState.position += it
            }
        }
    }
}

private operator fun WindowPosition.plus(dpOffset: DpOffset): WindowPosition {
    return WindowPosition(x = x + dpOffset.x, y = y + dpOffset.y)
}

@Composable
fun ArrowPad(onChangeOffset: (DpOffset) -> Unit) {
    Box(Modifier.size(300.dp)) {
        BtnIcon(Alignment.TopCenter, Icons.Filled.KeyboardArrowUp) {
            onChangeOffset(DpOffset(0.dp, -10.dp))
        }
        BtnIcon(Alignment.BottomCenter, Icons.Filled.KeyboardArrowDown) {
            onChangeOffset(DpOffset(0.dp, 10.dp))
        }
        BtnIcon(Alignment.CenterStart, Icons.Filled.KeyboardArrowLeft) {
            onChangeOffset(DpOffset(-10.dp, 0.dp))
        }
        BtnIcon(Alignment.CenterEnd, Icons.Filled.KeyboardArrowRight) {
            onChangeOffset(DpOffset(10.dp, 0.dp))
        }
    }
}

@Composable
fun BoxScope.BtnIcon(align: Alignment, icon: ImageVector, onClick: () -> Unit) {
    IconButton(modifier = Modifier.align(align),
        content = { Icon(icon, null) },
        onClick = {
            onClick()
        })
}
