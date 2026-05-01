package com.example.angatkinmirea.module4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class CompressWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val fileName = inputData.getString("file") ?: return Result.failure()

            // Симулируем прогресс
            for (i in 1..10) {
                delay(200)
                setProgress(workDataOf("step" to "compress", "progress" to i * 10))
            }

            val outputFileName = "compressed_$fileName"
            Result.success(workDataOf("file" to outputFileName))
        } catch (e: Exception) {
            Result.failure()
        }
    }
}