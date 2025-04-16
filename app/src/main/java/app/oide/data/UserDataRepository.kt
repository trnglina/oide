package app.oide.data

import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import app.oide.UserData
import kotlinx.coroutines.flow.firstOrNull

class UserDataRepository(private val userDataStore: DataStore<UserData>) {
    companion object {
        const val TAG = "UserDataRepository"
    }

    suspend fun getLastFile(): Uri? {
        return userDataStore.data.firstOrNull()?.lastFile?.toUri()
    }

    suspend fun storeLastFile(path: Uri?) {
        userDataStore.updateData { current ->
            var builder = current.toBuilder()

            builder = if (path != null) {
                builder.setLastFile(path.toString())
            } else {
                builder.clearLastFile()
            }

            builder.build()
        }
    }
}
