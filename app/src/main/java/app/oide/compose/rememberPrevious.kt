package app.oide.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember


@Composable
fun <T> rememberPrevious(current: T): T? {
    val ref = remember {
        object : MutableState<T?> {
            override var value: T? = null
            override fun component1(): T? = value
            override fun component2(): (T?) -> Unit = { value = it }
        }
    }

    SideEffect {
        if (ref.value != current) {
            ref.value = current
        }
    }

    return ref.value
}
