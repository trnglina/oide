package app.oide.data

import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import app.oide.Settings
import kotlinx.coroutines.flow.firstOrNull

class SettingsRepository(private val settingsStore: DataStore<Settings>) {
    private val TAG: String = "SettingsRepository"

    suspend fun getLastFile(): Uri? {
        return settingsStore.data.firstOrNull()?.lastFile?.toUri()
    }

    suspend fun storeLastFile(path: Uri?) {
        settingsStore.updateData { current ->
            var builder = current.toBuilder();

            builder = if (path != null) {
                builder.setLastFile(path.toString())
            } else {
                builder.clearLastFile()
            }

            builder.build()
        }
    }
}
