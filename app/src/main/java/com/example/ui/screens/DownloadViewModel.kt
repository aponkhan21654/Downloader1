package com.example.ui.screens

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.DownloadRecord
import com.example.data.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = DownloadRepository(application, database.downloadDao())

    private val _inputUrl = MutableStateFlow("")
    val inputUrl: StateFlow<String> = _inputUrl.asStateFlow()

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Configurable cobalt instance. Standard defaults to api.cobalt.tools
    private val _cobaltApiUrl = MutableStateFlow("https://api.cobalt.tools")
    val cobaltApiUrl: StateFlow<String> = _cobaltApiUrl.asStateFlow()

    private val _videoQuality = MutableStateFlow("1080")
    val videoQuality: StateFlow<String> = _videoQuality.asStateFlow()

    private val _audioOnly = MutableStateFlow(false)
    val audioOnly: StateFlow<Boolean> = _audioOnly.asStateFlow()

    // Control Telegram pop-up display on first entry
    private val _showTelegramPopup = MutableStateFlow(true)
    val showTelegramPopup: StateFlow<Boolean> = _showTelegramPopup.asStateFlow()

    // List of active downloads currently tracked by polling
    private val activeTrackingIds = mutableSetOf<Long>()

    val downloadsHistory: StateFlow<List<DownloadRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Automatically start polling for any downloads that were left DOWNLOADING or PENDING
        viewModelScope.launch {
            val active = repository.getActiveDownloads()
            active.forEach { record ->
                if (record.downloadId != -1L) {
                    activeTrackingIds.add(record.downloadId)
                }
            }
            if (activeTrackingIds.isNotEmpty()) {
                startProgressTracker()
            }
        }
    }

    fun updateInputUrl(url: String) {
        _inputUrl.value = url
    }

    fun clearInputUrl() {
        _inputUrl.value = ""
    }

    fun updateCobaltApiUrl(url: String) {
        _cobaltApiUrl.value = url
    }

    fun updateVideoQuality(quality: String) {
        _videoQuality.value = quality
    }

    fun updateAudioOnly(audio: Boolean) {
        _audioOnly.value = audio
    }

    fun dismissTelegramPopup() {
        _showTelegramPopup.value = false
    }

    fun showTelegramPopupAgain() {
        _showTelegramPopup.value = true
    }

    // Safely extract URL from text (e.g. if shared text has extra titles/descriptions)
    fun extractUrl(text: String): String? {
        val urlRegex = "https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*"
        val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group()
        } else {
            text.trim().takeIf { it.startsWith("http://") || it.startsWith("https://") }
        }
    }

    // Process extraction and download
    fun extractAndDownload(rawUrl: String) {
        val cleanUrl = extractUrl(rawUrl)
        if (cleanUrl == null) {
            _errorMessage.value = "Please enter or share a valid URL starting with http:// or https://"
            return
        }

        viewModelScope.launch {
            _isExtracting.value = true
            _errorMessage.value = null
            _successMessage.value = null
            
            val platform = repository.detectPlatform(cleanUrl)
            
            try {
                val response = repository.extractMedia(
                    apiEndpoint = _cobaltApiUrl.value,
                    url = cleanUrl,
                    quality = _videoQuality.value,
                    audioOnly = _audioOnly.value
                )

                when (response.status) {
                    "stream", "redirect" -> {
                        val dlUrl = response.url
                        if (dlUrl != null) {
                            val systemId = repository.enqueueSystemDownload(
                                downloadUrl = dlUrl,
                                suggestedFilename = response.filename,
                                originalUrl = cleanUrl,
                                platform = platform
                            )
                            
                            val record = DownloadRecord(
                                url = cleanUrl,
                                downloadUrl = dlUrl,
                                title = response.filename ?: "AponDownloader_${platform}_${System.currentTimeMillis()}",
                                platform = platform,
                                downloadId = systemId,
                                status = "DOWNLOADING"
                            )
                            repository.insertRecord(record)
                            
                            activeTrackingIds.add(systemId)
                            startProgressTracker()
                            
                            _successMessage.value = "Download started successfully!"
                            _inputUrl.value = "" // clear input on success
                        } else {
                            _errorMessage.value = "Failed to fetch download link. URL was empty."
                        }
                    }
                    "picker" -> {
                        val pickerItems = response.picker
                        if (!pickerItems.isNullOrEmpty()) {
                            _successMessage.value = "Started downloading ${pickerItems.size} items!"
                            pickerItems.forEachIndexed { index, item ->
                                val systemId = repository.enqueueSystemDownload(
                                    downloadUrl = item.url,
                                    suggestedFilename = "AponDownloader_${platform}_Item_${index + 1}_${System.currentTimeMillis()}",
                                    originalUrl = cleanUrl,
                                    platform = platform
                                )
                                val record = DownloadRecord(
                                    url = cleanUrl,
                                    downloadUrl = item.url,
                                    title = "Item ${index + 1} (${platform})",
                                    platform = platform,
                                    downloadId = systemId,
                                    status = "DOWNLOADING"
                                )
                                repository.insertRecord(record)
                                activeTrackingIds.add(systemId)
                            }
                            startProgressTracker()
                            _inputUrl.value = ""
                        } else {
                            _errorMessage.value = "No media items found in this slideshow/picker."
                        }
                    }
                    "error" -> {
                        val errorText = response.text ?: response.error?.text ?: "Unknown extraction error."
                        _errorMessage.value = "API Error: $errorText"
                    }
                    else -> {
                        _errorMessage.value = "Unexpected server response. Status: ${response.status}"
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadViewModel", "Error in download flow", e)
                _errorMessage.value = "Connection failed: ${e.localizedMessage ?: "Unknown error"}. Make sure the Cobalt server is online."
            } finally {
                _isExtracting.value = false
            }
        }
    }

    // Background poller to query Android's DownloadManager for active downloads
    private var isTracking = false
    private fun startProgressTracker() {
        if (isTracking) return
        isTracking = true
        
        viewModelScope.launch(Dispatchers.IO) {
            while (activeTrackingIds.isNotEmpty()) {
                val downloadManager = getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val iterator = activeTrackingIds.iterator()
                
                while (iterator.hasNext()) {
                    val downloadId = iterator.next()
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor? = downloadManager.query(query)
                    
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val bytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        
                        if (statusIdx != -1 && bytesIdx != -1 && totalIdx != -1) {
                            val systemStatus = cursor.getInt(statusIdx)
                            val bytesDownloaded = cursor.getLong(bytesIdx)
                            val totalBytes = cursor.getLong(totalIdx)
                            
                            val progress = if (totalBytes > 0) {
                                ((bytesDownloaded * 100) / totalBytes).toInt()
                            } else {
                                0
                            }
                            
                            val record = repository.getRecordByDownloadId(downloadId)
                            if (record != null) {
                                when (systemStatus) {
                                    DownloadManager.STATUS_SUCCESSFUL -> {
                                        repository.updateRecord(record.copy(
                                            status = "COMPLETED",
                                            progress = 100,
                                            fileSize = totalBytes
                                        ))
                                        iterator.remove()
                                    }
                                    DownloadManager.STATUS_FAILED -> {
                                        repository.updateRecord(record.copy(
                                            status = "FAILED",
                                            progress = progress
                                        ))
                                        iterator.remove()
                                    }
                                    DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED -> {
                                        repository.updateRecord(record.copy(
                                            status = "DOWNLOADING",
                                            progress = progress,
                                            fileSize = totalBytes
                                        ))
                                    }
                                    DownloadManager.STATUS_PENDING -> {
                                        repository.updateRecord(record.copy(
                                            status = "PENDING",
                                            progress = 0
                                        ))
                                    }
                                }
                            }
                        }
                    } else {
                        // Download not found in manager (cleared or cancelled)
                        val record = repository.getRecordByDownloadId(downloadId)
                        if (record != null && record.status == "DOWNLOADING") {
                            repository.updateRecord(record.copy(status = "FAILED"))
                        }
                        iterator.remove()
                    }
                    cursor?.close()
                }
                
                delay(1500) // Poll every 1.5 seconds
            }
            isTracking = false
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }
}
