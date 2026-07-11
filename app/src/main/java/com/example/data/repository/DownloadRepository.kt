package com.example.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.data.database.DownloadDao
import com.example.data.database.DownloadRecord
import com.example.data.network.CobaltApiService
import com.example.data.network.CobaltRequest
import com.example.data.network.CobaltResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.cobalt.tools/") // Required placeholder, overridden by @Url
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(CobaltApiService::class.java)

    val allRecords: Flow<List<DownloadRecord>> = downloadDao.getAllRecords()

    suspend fun insertRecord(record: DownloadRecord): Long = withContext(Dispatchers.IO) {
        downloadDao.insertRecord(record)
    }

    suspend fun updateRecord(record: DownloadRecord) = withContext(Dispatchers.IO) {
        downloadDao.updateRecord(record)
    }

    suspend fun deleteRecordById(id: Int) = withContext(Dispatchers.IO) {
        downloadDao.deleteRecordById(id)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        downloadDao.clearAllRecords()
    }

    suspend fun getRecordByDownloadId(downloadId: Long): DownloadRecord? = withContext(Dispatchers.IO) {
        downloadDao.getRecordByDownloadId(downloadId)
    }

    suspend fun getActiveDownloads(): List<DownloadRecord> = withContext(Dispatchers.IO) {
        downloadDao.getActiveDownloads()
    }

    // Call Cobalt API to extract the direct watermark-free download link
    suspend fun extractMedia(
        apiEndpoint: String,
        url: String,
        quality: String = "1080",
        audioOnly: Boolean = false
    ): CobaltResponse = withContext(Dispatchers.IO) {
        val request = CobaltRequest(
            url = url,
            videoQuality = quality,
            audioOnly = audioOnly,
            filenamePattern = "classic"
        )
        // Ensure endpoint ends with a trailing slash if passed as a domain
        var endpoint = apiEndpoint
        if (!endpoint.endsWith("/")) {
            endpoint += "/"
        }
        
        Log.d("DownloadRepository", "Extracting url: $url using endpoint: $endpoint")
        apiService.downloadMedia(endpoint, request)
    }

    // Enqueue the direct stream download link in Android's system DownloadManager
    fun enqueueSystemDownload(
        downloadUrl: String,
        suggestedFilename: String?,
        originalUrl: String,
        platform: String
    ): Long {
        val extension = if (downloadUrl.contains(".mp3") || downloadUrl.contains("audio")) "mp3" else "mp4"
        val timestamp = System.currentTimeMillis()
        val filename = suggestedFilename?.takeIf { it.isNotBlank() }
            ?: "AponDownloader_${platform}_${timestamp}.$extension"

        Log.d("DownloadRepository", "Enqueuing system download: $downloadUrl as $filename")
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(filename)
            .setDescription("Watermark-free download from $platform")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request)
    }

    // Helper utility to detect the target platform from a URL
    fun detectPlatform(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("instagram.com") -> "Instagram"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> "Facebook"
            else -> "Social Media"
        }
    }
}
