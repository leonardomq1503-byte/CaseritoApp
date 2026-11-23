package com.example.caseritoapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.caseritoapp.CaseritoApplication

class CaseritoSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val expectedAuthUserId = inputData.getString(AUTH_USER_ID) ?: return Result.failure()
        val application = applicationContext as CaseritoApplication
        return try {
            val identity = application.authRepository.getOrSyncCurrentIdentity() ?: return Result.success()
            if (identity.authUserId != expectedAuthUserId) return Result.success()
            application.personalDataSyncRepository.sincronizar(identity.authUserId, identity.localUserId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val AUTH_USER_ID = "auth_user_id"
    }
}
