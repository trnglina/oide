package app.oide

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import app.oide.data.SettingsSerializer

val Context.settingsStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer,
)
