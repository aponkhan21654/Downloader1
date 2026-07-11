package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_records")
data class DownloadRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val downloadUrl: String?,
    val title: String,
    val platform: String,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadId: Long = -1L,
    val status: String = "PENDING", // PENDING, DOWNLOADING, COMPLETED, FAILED
    val progress: Int = 0,
    val filePath: String? = null,
    val fileSize: Long = 0L
)
