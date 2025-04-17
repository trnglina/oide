package app.oide

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import app.oide.ui.Editor
import app.oide.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LOW_PROFILE)


        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel<MainViewModel>(factory = MainViewModel.Factory(this))
            val state = viewModel.state.collectAsState()

            AppTheme {
                Editor(
                    textFieldState = state.value.textFieldState,
                    filePath = state.value.filePath,

                    onSaveAs = { uri -> viewModel.saveToFile(uri) },
                    onOpen = { uri -> viewModel.loadFromFile(uri) },
                    onSave = { viewModel.save() },
                    onNew = { viewModel.new() },
                )
            }
        }
    }
}
