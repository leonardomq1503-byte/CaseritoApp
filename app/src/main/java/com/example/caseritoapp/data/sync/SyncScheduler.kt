package com.example.caseritoapp.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object SyncScheduler {
    fun enqueue(context: Context, authUserId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<CaseritoSyncWorker>()
            .setInputData(workDataOf(CaseritoSyncWorker.AUTH_USER_ID to authUserId))
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "caserito-personal-data-sync-$authUserId",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )

        val periodic = PeriodicWorkRequestBuilder<CaseritoSyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf(CaseritoSyncWorker.AUTH_USER_ID to authUserId))
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )
    }

    fun cancelPeriodic(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    private const val PERIODIC_WORK_NAME = "caserito-personal-data-sync-periodic"
}
