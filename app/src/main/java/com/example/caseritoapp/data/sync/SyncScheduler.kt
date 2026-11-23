package com.example.caseritoapp.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

object SyncScheduler {
    fun enqueue(context: Context, authUserId: String) {
        val request = OneTimeWorkRequestBuilder<CaseritoSyncWorker>()
            .setInputData(workDataOf(CaseritoSyncWorker.AUTH_USER_ID to authUserId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "caserito-personal-data-sync-$authUserId",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}
