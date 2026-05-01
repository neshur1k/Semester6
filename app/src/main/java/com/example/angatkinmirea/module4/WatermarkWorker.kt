package com.example.angatkinmirea.module4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class WatermarkWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val fileName = inputData.getString("file") ?: return Result.failure()

            for (i in 1..10) {
                delay(200)
                setProgress(workDataOf("step" to "watermark", "progress" to i * 10))
            }

            val outputFileName = "watermarked_$fileName"
            Result.success(workDataOf("file" to outputFileName))
        } catch (e: Exception) {
            Result.failure()
        }
    }
}