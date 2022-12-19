package com.udacity

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.database.Cursor
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val context = application

    val progress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private var downloadID: Long = 0

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }

    init {
        progress.value = 0
    }

    // Download
    @SuppressLint("Range")
    fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
//                .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
                .setTitle(R.string.app_name.toString()) // Title of the Download Notification
                .setDescription(/*"Downloading"*/R.string.app_description.toString()) // Description of the Download Notification
                .setRequiresCharging(false) // Set if charging is required to begin the download
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // Enqueue puts the download request in the queue.

        viewModelScope.launch {
            var finishDownload = false
            while (!finishDownload) {
                val cursor: Cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            finishDownload = true
                        }
                        DownloadManager.STATUS_PAUSED -> {}
                        DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_RUNNING -> {
                            val total =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (total >= 0) {
                                val downloaded =
                                    cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                progress.value = (downloaded * 100L / total).toInt()
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            progress.value = 100
                            finishDownload = true
                        }
                    }
                }
            }
        }
    }

    fun getDownloadId() = downloadID
}