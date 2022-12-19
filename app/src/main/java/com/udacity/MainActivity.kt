package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.util.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

enum class RadioGroupSelect { GLIDE, Repository, Retrofit }

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        var radioGroupSelect : RadioGroupSelect = RadioGroupSelect.GLIDE
        radio_group.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
            radioGroupSelect = when (i) {
                R.id.glide_radio_button -> RadioGroupSelect.GLIDE
                R.id.current_repository_radio_button -> RadioGroupSelect.Repository
                else -> RadioGroupSelect.Retrofit
            }
        }

        custom_button.setOnClickListener {
            Toast.makeText(this, radioGroupSelect.toString(), Toast.LENGTH_SHORT).show()
            val notificationManager = ContextCompat.getSystemService(
                application,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.sendNotification(application.getString(R.string.notification_description), application)
            download(radioGroupSelect)
        }

        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    private fun download(radioGroupSelect: RadioGroupSelect) {
//        val request =
//            DownloadManager.Request(Uri.parse(URL))
//                .setTitle(getString(R.string.app_name))
//                .setDescription(getString(R.string.app_description))
//                .setRequiresCharging(false)
//                .setAllowedOverMetered(true)
//                .setAllowedOverRoaming(true)
//
//        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//        downloadID =
//            downloadManager.enqueue(request) // Enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

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
}
