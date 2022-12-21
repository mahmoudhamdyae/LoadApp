package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.udacity.util.RadioGroupSelect
import com.udacity.util.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroupSelect: RadioGroupSelect
    private val viewModel: MainViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Checking if the received broadcast is for our enqueued download by matching download id
            if (viewModel.getDownloadId() == id) {
                custom_button.buttonState = ButtonState.Completed
                createNotification()

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                var status = ""
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    status =
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index))
                            getString(R.string.success)
                        else
                            getString(R.string.fail)
                }

                val sharedPref = application.getSharedPreferences(
                    getString(R.string.key1), Context.MODE_PRIVATE
                ) ?: return
                with(sharedPref.edit()) {
                    putString(getString(R.string.key1), radioGroupSelect.toString())
                    putString(getString(R.string.key2), status)
                    apply()
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
        radio_group.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
            radioGroupSelect = when (i) {
                R.id.glide_radio_button -> RadioGroupSelect.GLIDE
                R.id.current_repository_radio_button -> RadioGroupSelect.REPOSITORY
                else -> RadioGroupSelect.RETROFIT
            }
        }

        // Custom Button
        custom_button.setOnClickListener {
            if (radioGroupSelect == RadioGroupSelect.NOACTION) {
                Toast.makeText(this, getString(R.string.select_one_toast), Toast.LENGTH_SHORT).show()
            } else {
                custom_button.buttonState = ButtonState.Loading
                viewModel.download()
            }
        }

        // Channel for Notifications
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )

        val observer = Observer<Int> {
//            test_text.text = it.toString()
//            determinateBar.progress = it
        }

        viewModel.progress.observe(this, observer)
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
