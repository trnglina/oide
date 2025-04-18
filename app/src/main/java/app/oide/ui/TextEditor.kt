package app.oide.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun TextEditor(
    contentPadding: PaddingValues,
    state: TextFieldState,
    focusRequester: FocusRequester,
) {
    val density = LocalDensity.current

    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()

    var lastSelectionTop = rememberSaveable { mutableFloatStateOf(0f) }
    var lastSelectionBottom = rememberSaveable { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(contentPadding)
    ) {
        BasicTextField(
            state,
            modifier = Modifier
                .fillMaxSize()
                .defaultMinSize(minHeight = with(density) { scrollState.viewportSize.toDp() } - topPadding - bottomPadding)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 1.65.em,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            onTextLayout = { getResult -> textLayoutResult = getResult() }
        )
    }

    LaunchedEffect(
        scrollState.viewportSize,
        state.selection.start,
        state.selection.end
    ) {
        val startCursorRect = runCatching {
            textLayoutResult?.getCursorRect(state.selection.start)
        }.getOrNull() ?: return@LaunchedEffect

        val endCursorRect = runCatching {
            textLayoutResult?.getCursorRect(state.selection.end)
        }.getOrNull() ?: return@LaunchedEffect

        val selectionTop = min(startCursorRect.top, endCursorRect.top)
        val selectionBottom = max(startCursorRect.bottom, endCursorRect.bottom)

        val topPadding = with(density) { topPadding.toPx() }
        val bottomPadding = with(density) { bottomPadding.toPx() }

        val scrollOffset = scrollState.value
        val scrollViewportHeight = scrollState.viewportSize

        val viewportTop = scrollOffset + topPadding
        val viewportBottom = scrollOffset + scrollViewportHeight - topPadding - bottomPadding

        val offset = when {
            selectionTop < lastSelectionTop.floatValue -> min(
                0f, selectionTop - viewportTop
            )

            selectionTop > lastSelectionTop.floatValue -> max(
                0f, selectionTop - viewportBottom
            )

            selectionBottom > lastSelectionBottom.floatValue -> max(
                0f, selectionBottom - viewportBottom
            )

            selectionBottom < lastSelectionBottom.floatValue -> min(
                0f, selectionBottom - viewportTop
            )

            else -> 0f
        }

        if (offset != 0f) {
            scrollState.scrollBy(offset)
        }

        lastSelectionTop.floatValue = selectionTop
        lastSelectionBottom.floatValue = selectionBottom
    }
}
