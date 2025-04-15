package app.oide

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import app.oide.data.FileSystemDocumentRepository
import app.oide.data.SettingsRepository
import app.oide.ui.screens.EditorScreen
import app.oide.ui.screens.EditorViewModel
import app.oide.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LOW_PROFILE)

        enableEdgeToEdge()
        setContent {
            val viewModel: EditorViewModel = viewModel(
                factory = EditorViewModel.Factory(
                    FileSystemDocumentRepository(contentResolver),
                    SettingsRepository(settingsStore),
                )
            )

            AppTheme {
                EditorScreen(viewModel)
            }
        }
    }
}
