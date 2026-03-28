package com.example.angatkinmirea

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.UUID
import androidx.work.ArrayCreatingInputMerger
import androidx.work.workDataOf

class PhotoWorkManager(context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun chainOneTimeSampleWork(fileName: String) {
        val compressRequest = OneTimeWorkRequestBuilder<CompressWorker>()
            .setInputData(workDataOf("file" to fileName))
            .addTag("PhotoChain")
            .build()

        val watermarkRequest = OneTimeWorkRequestBuilder<WatermarkWorker>()
            .addTag("PhotoChain")
            .build()

        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag("PhotoChain")
            .build()

        workManager.cancelUniqueWork("UniqueWorkTag")

        workManager.beginUniqueWork(
            "UniqueWorkTag",
            ExistingWorkPolicy.REPLACE,
            compressRequest
        )
        .then(watermarkRequest)
        .then(uploadRequest)
        .enqueue()
    }
}