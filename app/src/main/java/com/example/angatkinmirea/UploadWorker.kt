package com.example.angatkinmirea

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class UploadWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val fileName = inputData.getString("file")
            for (i in 1..10) {
                delay(200)
                setProgress(
                    workDataOf(
                        "step" to "upload",
                        "progress" to i * 10
                    )
                )
            }
            val output = workDataOf("file" to "uploaded_$fileName")
            Result.success(output)
        } catch (e: Exception) {
            Result.failure()
        }
    }
}