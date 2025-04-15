package app.oide

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import app.oide.data.FileSystemDocumentRepository
import app.oide.data.SettingsRepository
import app.oide.data.SettingsSerializer
import app.oide.ui.screens.EditorScreen
import app.oide.ui.screens.EditorViewModel
import app.oide.ui.theme.AppTheme

val Context.settingsStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LOW_PROFILE)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                EditorScreen(viewModel {
                    val documentRepository = FileSystemDocumentRepository(contentResolver)
                    val settingsRepository = SettingsRepository(settingsStore)

                    EditorViewModel(documentRepository, settingsRepository)
                })
            }
        }
    }
}
