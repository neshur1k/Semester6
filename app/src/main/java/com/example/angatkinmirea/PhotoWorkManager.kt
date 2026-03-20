package com.example.angatkinmirea

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.UUID

class PhotoWorkManager(context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun chainOneTimeSampleWork(fileName: String): UUID {
        val compressRequest = OneTimeWorkRequestBuilder<CompressWorker>()
            .setInputData(transferDataObject(fileName))
            .addTag("PhotoChain")
            .build()
        val watermarkRequest = OneTimeWorkRequestBuilder<WatermarkWorker>()
            .addTag("PhotoChain")
            .build()
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag("PhotoChain")
            .build()

        workManager
            .beginUniqueWork(
                "UniqueWorkTag",
                ExistingWorkPolicy.REPLACE,
                compressRequest
            )
            .then(watermarkRequest)
            .then(uploadRequest)
            .enqueue()

        return compressRequest.id
    }

    private fun transferDataObject(
        fileName: String
    ): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putString("file", fileName)
        return dataBuilder.build()
    }
}