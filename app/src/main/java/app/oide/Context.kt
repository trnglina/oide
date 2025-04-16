package app.oide

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import app.oide.data.UserDataSerializer

val Context.userDataStore: DataStore<UserData> by dataStore(
    fileName = "userData.pb",
    serializer = UserDataSerializer,
)
