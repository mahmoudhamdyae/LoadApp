package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

enum class RadioGroupSelect { GLIDE, REPOSITORY, RETROFIT, NOACTION }

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroupSelect: RadioGroupSelect

    private var downloadID: Long = 0
    private lateinit var url: String

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val intentId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (intentId == -1L)
                return
            intentId?.let { id ->
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status =
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index))
                            getString(R.string.success)
                        else
                            getString(R.string.fail)
                    // Save in Shared Preference
                    val sharedPref = application.getSharedPreferences(
                        getString(R.string.key1), Context.MODE_PRIVATE
                    ) ?: return
                    with(sharedPref.edit()) {
                        putString(getString(R.string.key1), radioGroupSelect.toString())
                        putString(getString(R.string.key2), status)
                        apply()
                    }
                    createNotification()
                    custom_button.buttonState = ButtonState.Completed
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Radio Buttons
        radioGroupSelect = RadioGroupSelect.NOACTION
        radio_group.setOnCheckedChangeListener { _: RadioGroup, index: Int ->
            radioGroupSelect = when (index) {
                R.id.glide_radio_button -> RadioGroupSelect.GLIDE
                R.id.current_repository_radio_button -> RadioGroupSelect.REPOSITORY
                else -> RadioGroupSelect.RETROFIT

            }
        }

        // URL Based on Radio Buttons Selection
        url = when (radioGroupSelect) {
            RadioGroupSelect.GLIDE -> GLIDE_URL
            RadioGroupSelect.REPOSITORY -> REPOSITORY_URL
            else -> RETROFIT_URL
        }

        // Custom Button
        custom_button.setOnClickListener {
            if (radioGroupSelect == RadioGroupSelect.NOACTION) {
                Toast.makeText(this, getString(R.string.select_one_toast), Toast.LENGTH_SHORT).show()
            } else {
                custom_button.buttonState = ButtonState.Loading
                download()
            }
        }

        // Channel for Notifications
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
    }

companion object {
    private const val GLIDE_URL =
        "https://github.com/bumptech/glide/archive/master.zip"
    private const val REPOSITORY_URL =
        "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    private const val RETROFIT_URL =
        "https://github.com/square/retrofit/archive/master.zip"
}

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(R.string.app_name.toString()) // Title of the Download Notification
                .setDescription(/*"Downloading"*/R.string.app_description.toString()) // Description of the Download Notification
                .setRequiresCharging(false) // Set if charging is required to begin the download
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // Enqueue puts the download request in the queue.
    }

    // Create Notification Channel
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_channel_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    // Create A Notification
    private fun createNotification() {
        val notificationManager = ContextCompat.getSystemService(
            application,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendNotification(application.getString(R.string.notification_description), application)
    }
}
