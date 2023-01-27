import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.JDialog
import kotlin.math.roundToInt

fun main() = application {
    val resourceValue = remember {
        javaClass.classLoader?.getResource("resource.txt")?.readText() ?: "missing resource"
    }

    var visible by remember { mutableStateOf(true) }

    Window(onCloseRequest = ::exitApplication) {
        if (visible) {
            Dialog(
                "Title",
                onCloseRequest = {},
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
    Dialog2(
        onCloseRequest = onCloseRequest,
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

@Composable
fun Dialog2(
    onCloseRequest: () -> Unit,
    state: DialogState = rememberDialogState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    onPreviewKeyEvent: ((KeyEvent) -> Boolean) = { false },
    onKeyEvent: ((KeyEvent) -> Boolean) = { false },
    content: @Composable DialogWindowScope.() -> Unit
) {
//    val owner = LocalWindow.current

    val currentState by rememberUpdatedState(state)
    val currentTitle by rememberUpdatedState(title)
    val currentIcon by rememberUpdatedState(icon)
    val currentUndecorated by rememberUpdatedState(undecorated)
    val currentTransparent by rememberUpdatedState(transparent)
    val currentResizable by rememberUpdatedState(resizable)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentFocusable by rememberUpdatedState(focusable)
    val currentOnCloseRequest by rememberUpdatedState(onCloseRequest)

    val updater = remember(::ComponentUpdater)

    // the state applied to the dialog. exist to avoid races between DialogState changes and the state stored inside the native dialog
    val appliedState = remember {
        object {
            var size: DpSize? = null
            var position: WindowPosition? = null
        }
    }

    Dialog(
        visible = visible,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        create = {
            val graphicsConfiguration = WindowLocationTracker.lastActiveGraphicsConfiguration
            val dialog = if (false/* && owner != null*/) {
//                ComposeDialog(owner, java.awt.Dialog.ModalityType.DOCUMENT_MODAL, graphicsConfiguration = graphicsConfiguration)
                TODO("")
            } else {
                ComposeDialog(graphicsConfiguration = graphicsConfiguration)
            }
            dialog.apply {
                // close state is controlled by DialogState.isOpen
                defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        currentOnCloseRequest()
                    }
                })
                addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent) {
                        currentState.size = DpSize(width.dp, height.dp)
                        appliedState.size = currentState.size
                    }

                    override fun componentMoved(e: ComponentEvent) {
                        currentState.position = WindowPosition(x.dp, y.dp)
                        appliedState.position = currentState.position
                    }
                })
                WindowLocationTracker.onWindowCreated(this)
            }
        },
        dispose = {
            WindowLocationTracker.onWindowDisposed(it)
            it.dispose()
        },
        update = { dialog ->
            updater.update {
                set(currentTitle, dialog::setTitle)
                set(currentIcon, dialog::setIcon)
                set(currentUndecorated, dialog::setUndecoratedSafely)
                set(currentTransparent, dialog::isTransparent::set)
                set(currentResizable, dialog::setResizable)
                set(currentEnabled, dialog::setEnabled)
                set(currentFocusable, dialog::setFocusableWindowState)
            }
            if (state.size != appliedState.size) {
                dialog.setSizeSafely(state.size)
                appliedState.size = state.size
            }
            if (state.position != appliedState.position) {
                dialog.setPositionSafely(
                    state.position,
                    platformDefaultPosition = { WindowLocationTracker.getCascadeLocationFor(dialog) }
                )
                appliedState.position = state.position
            }
        },
        content = content
    )
}


internal object WindowLocationTracker {
    private val cascadeOffset = Point(48, 48)

    private var lastFocusedWindows = mutableSetOf<Window>()

    private val focusListener = object : WindowFocusListener {
        override fun windowGainedFocus(e: WindowEvent) {
            // put window on the top of the set
            lastFocusedWindows.remove(e.window)
            lastFocusedWindows.add(e.window)
        }

        override fun windowLostFocus(e: WindowEvent) = Unit
    }

    fun onWindowCreated(window: Window) {
        window.addWindowFocusListener(focusListener)
    }

    fun onWindowDisposed(window: Window) {
        window.removeWindowFocusListener(focusListener)
        lastFocusedWindows.remove(window)
    }

    val lastActiveGraphicsConfiguration: GraphicsConfiguration? get() =
        lastFocusedWindows.lastOrNull()?.graphicsConfiguration

    fun getCascadeLocationFor(window: Window): Point {
        val lastWindow = lastFocusedWindows.lastOrNull()
        val graphicsConfiguration = lastWindow?.graphicsConfiguration ?:
        GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice?.defaultConfiguration

        return if (graphicsConfiguration != null) {
            val screenBounds = graphicsConfiguration.bounds
            val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
            val screenLeftTop = screenBounds.leftTop + Point(screenInsets.left, screenInsets.top)
            val screenRightBottom = screenBounds.rightBottom - Point(screenInsets.right, screenInsets.bottom)

            val lastLocation = lastWindow?.location ?: screenLeftTop
            var location = lastLocation + cascadeOffset
            val rightBottom = location + window.size.rightBottom
            if (rightBottom.x > screenRightBottom.x || rightBottom.y > screenRightBottom.y) {
                location = screenLeftTop + cascadeOffset
            }
            location
        } else {
            cascadeOffset
        }
    }
}

internal val Dimension.rightBottom get() = Point(width, height)
internal operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)
internal operator fun Point.minus(other: Point) = Point(x - other.x, y - other.y)

internal val Rectangle.leftTop get() = Point(x, y)
internal val Rectangle.rightBottom get() = Point(x + width, y + height)


internal class ComponentUpdater {
    private var updatedValues = mutableListOf<Any?>()

    fun update(body: UpdateScope.() -> Unit) {
        UpdateScope().body()
    }

    inner class UpdateScope {
        private var index = 0

        /**
         * Compare [value] with the old one and if it is changed - store a new value and call
         * [update]
         */
        fun <T : Any?> set(value: T, update: (T) -> Unit) {
            if (index < updatedValues.size) {
                if (updatedValues[index] != value) {
                    update(value)
                    updatedValues[index] = value
                }
            } else {
                check(index == updatedValues.size)
                update(value)
                updatedValues.add(value)
            }

            index++
        }
    }
}


internal fun Window.setSizeSafely(size: DpSize) {
    val screenBounds by lazy { graphicsConfiguration.bounds }

    val isWidthSpecified = size.isSpecified && size.width.isSpecified
    val isHeightSpecified = size.isSpecified && size.height.isSpecified

    val width = if (isWidthSpecified) {
        size.width.value.roundToInt().coerceAtLeast(0)
    } else {
        screenBounds.width
    }

    val height = if (isHeightSpecified) {
        size.height.value.roundToInt().coerceAtLeast(0)
    } else {
        screenBounds.height
    }

    if (!isWidthSpecified || !isHeightSpecified) {
        preferredSize = Dimension(width, height)
        pack()
        // if we set null, getPreferredSize will return the default inner size determined by
        // the inner components (see the description of setPreferredSize)
        preferredSize = null
    }

    setSize(
        if (isWidthSpecified) width else preferredSize.width,
        if (isHeightSpecified) height else preferredSize.height,
    )
}


internal fun Window.setPositionSafely(
    position: WindowPosition,
    platformDefaultPosition: () -> Point?
) = when (position) {
    WindowPosition.PlatformDefault -> location = platformDefaultPosition()
    is WindowPosition.Aligned -> align(position.alignment)
    is WindowPosition.Absolute -> setLocation(
        position.x.value.roundToInt(),
        position.y.value.roundToInt()
    )
}

internal fun Window.setIcon(painter: Painter?) {
    setIconImage(painter?.toAwtImage(density, layoutDirection, iconSize))
}

internal fun Dialog.setUndecoratedSafely(value: Boolean) {
    if (this.isUndecorated != value) {
        this.isUndecorated = value
    }
}

internal fun Window.align(alignment: Alignment) {
    val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
    val screenBounds = graphicsConfiguration.bounds
    val size = IntSize(size.width, size.height)
    val screenSize = IntSize(
        screenBounds.width - screenInsets.left - screenInsets.right,
        screenBounds.height - screenInsets.top - screenInsets.bottom
    )
    val location = alignment.align(size, screenSize, LayoutDirection.Ltr)

    setLocation(
        screenBounds.x + screenInsets.left + location.x,
        screenBounds.y + screenInsets.top + location.y
    )
}

internal val Component.density: Density get() = graphicsConfiguration.density

internal val Component.layoutDirection: LayoutDirection
    get() = this.locale.layoutDirection

private val iconSize = Size(32f, 32f)

internal val Locale.layoutDirection: LayoutDirection
    get() = if (ComponentOrientation.getOrientation(this).isLeftToRight) {
        LayoutDirection.Ltr
    } else {
        LayoutDirection.Rtl
    }

private val GraphicsConfiguration.density: Density get() = Density(
    defaultTransform.scaleX.toFloat(),
    fontScale = 1f
)
