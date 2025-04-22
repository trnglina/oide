package app.oide.ui

import androidx.compose.foundation.ScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.oide.compose.rememberPrevious

fun calculateSelectionBounds(
    range: TextRange,
    textLayoutResult: TextLayoutResult?
): Pair<Float, Float> {
    if (textLayoutResult == null) return Pair(0f, 0f)

    return runCatching {
        val selectionTop = textLayoutResult.getCursorRect(range.min).top
        val selectionBottom = textLayoutResult.getCursorRect(range.max).bottom

        Pair(selectionTop, selectionBottom)
    }.getOrDefault(Pair(0f, 0f))
}

fun calculateViewportBounds(
    scrollState: ScrollState,
    density: Density,
    paddingValues: PaddingValues
): Pair<Float, Float> {
    val topPaddingPx = with(density) { paddingValues.calculateTopPadding().toPx() }
    val bottomPaddingPx = with(density) { paddingValues.calculateBottomPadding().toPx() }

    val viewportTop = scrollState.value + topPaddingPx
    val viewportBottom =
        scrollState.value + scrollState.viewportSize - topPaddingPx - bottomPaddingPx

    return Pair(viewportTop, viewportBottom)
}

@Composable
fun TextEditor(
    contentPadding: PaddingValues,
    state: TextFieldState,
    focusRequester: FocusRequester,
) {
    val density = LocalDensity.current

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()

    val minHeight = with(density) {
        scrollState.viewportSize.toDp()
    } - contentPadding.calculateTopPadding() - contentPadding.calculateBottomPadding()

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
                .defaultMinSize(minHeight = minHeight)
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

    val (selectionTop, selectionBottom) =
        calculateSelectionBounds(state.selection, textLayoutResult)

    val (viewportTop, viewportBottom) =
        calculateViewportBounds(scrollState, density, contentPadding)

    val selection = state.selection
    val previousSelection =
        rememberPrevious(selection) ?: TextRange(0)

    LaunchedEffect(selection) {
        if (selectionTop == 0f && selectionBottom == 0f) return@LaunchedEffect

        val offset = when {
            selection.min < previousSelection.min && selectionTop < viewportTop -> selectionTop - viewportTop
            selection.min > previousSelection.min && selectionTop > viewportBottom -> selectionTop - viewportBottom
            selection.max < previousSelection.max && selectionBottom < viewportTop -> selectionBottom - viewportTop
            selection.max > previousSelection.max && selectionBottom > viewportBottom -> selectionBottom - viewportBottom

            else -> 0f
        }

        if (offset != 0f) {
            scrollState.scrollBy(offset)
        }
    }

    val (previousViewportTop, previousViewportBottom) =
        rememberPrevious(Pair(viewportTop, viewportBottom)) ?: Pair(0f, 0f)

    LaunchedEffect(scrollState.viewportSize) {
        if (selectionTop == 0f && selectionBottom == 0f) return@LaunchedEffect

        val offset = when {
            viewportBottom < previousViewportBottom && selectionBottom > viewportBottom -> selectionBottom - viewportBottom
            viewportTop > previousViewportTop && selectionTop < viewportTop -> selectionTop - viewportTop

            else -> 0f
        }

        if (offset != 0f) {
            scrollState.scrollBy(offset)
        }
    }
}
