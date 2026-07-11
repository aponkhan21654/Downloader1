package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DownloadRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DownloadRecord): Long

    @Update
    suspend fun updateRecord(record: DownloadRecord)

    @Delete
    suspend fun deleteRecord(record: DownloadRecord)

    @Query("DELETE FROM download_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM download_records")
    suspend fun clearAllRecords()

    @Query("SELECT * FROM download_records WHERE downloadId = :downloadId LIMIT 1")
    suspend fun getRecordByDownloadId(downloadId: Long): DownloadRecord?

    @Query("SELECT * FROM download_records WHERE status = 'DOWNLOADING' OR status = 'PENDING'")
    suspend fun getActiveDownloads(): List<DownloadRecord>
}
