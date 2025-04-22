package app.oide

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.oide.ui.screens.Editor
import app.oide.ui.screens.EditorScreen
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
            val navController = rememberNavController()

            AppTheme {
                NavHost(
                    navController = navController,
                    startDestination = Editor(id = 0)
                ) {
                    composable<Editor> { EditorScreen() }
                }
            }
        }
    }
}
