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

class MyDialogState(positionState: MutableState<WindowPosition>, sizeState: MutableState<DpSize>) : DialogState {
    override var position: WindowPosition by positionState
    override var size: DpSize by sizeState
}

fun main() = application {
    var dialogVisible by remember { mutableStateOf(true) }
    var position by remember { mutableStateOf(DpOffset(200.dp, 200.dp)) }

    Window(onCloseRequest = ::exitApplication) {
        Button(onClick = {
            dialogVisible = true
        }) {
            Text("Open Dialog")
        }
        key(position) {
            val dialogState: DialogState =
                rememberDialogState(WindowPosition(position.x, position.y), DpSize(400.dp, 400.dp))
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
                    Box(Modifier.size(150.dp)) {
                        BtnIcon(Alignment.TopCenter, Icons.Default.KeyboardArrowUp) {
                            position += DpOffset(0.dp, -10.dp)
                        }
                        BtnIcon(Alignment.BottomCenter, Icons.Default.KeyboardArrowDown) {
                            position += DpOffset(0.dp, 10.dp)
                        }
                        BtnIcon(Alignment.CenterStart, Icons.Default.KeyboardArrowLeft) {
                            position += DpOffset(-10.dp, 0.dp)
                        }
                        BtnIcon(Alignment.CenterEnd, Icons.Default.KeyboardArrowRight) {
                            position += DpOffset(10.dp, 0.dp)
                        }
                        Text("Move", Modifier.align(Alignment.Center))
                    }
                    Column(Modifier.align(Alignment.CenterEnd)) {
                        Button({
                            dialogState.size = dialogState.size.copy(width = dialogState.size.width + 10.dp)
                        }) { Text("+") }
                        Button({
                            dialogState.size = dialogState.size.copy(width = dialogState.size.width - 10.dp)
                        }) { Text("-") }
                    }
                    Row(Modifier.align(Alignment.BottomCenter)) {
                        Button({
                            dialogState.size = dialogState.size.copy(height = dialogState.size.height + 10.dp)
                        }) { Text("+") }
                        Button({
                            dialogState.size = dialogState.size.copy(height = dialogState.size.height - 10.dp)
                        }) { Text("-") }
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.BtnIcon(align: Alignment, icon: ImageVector, onClick: () -> Unit) {
    IconButton(modifier = Modifier.align(align),
        content = { Icon(icon, null) },
        onClick = {

        })
}
