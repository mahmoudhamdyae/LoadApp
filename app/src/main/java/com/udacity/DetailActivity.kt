package com.udacity

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val sharedPref = application.getSharedPreferences(
            getString(R.string.key1),Context.MODE_PRIVATE) ?: return
        val fileNameSaved = sharedPref.getString(getString(R.string.key1), "")
        val statusSaved = sharedPref.getString(getString(R.string.key2), "")

        // File Name Text
        file_name.text = when(fileNameSaved) {
            RadioGroupSelect.GLIDE.toString() -> getString(R.string.radio_button1_text)
            RadioGroupSelect.REPOSITORY.toString() -> getString(R.string.radio_button2_text)
            else -> getString(R.string.radio_button3_text)
        }

        // Status Text
        status.text = statusSaved
        status.setTextColor(when(statusSaved) {
            getString(R.string.success) -> ContextCompat.getColor(this, R.color.success)
            else -> ContextCompat.getColor(this, R.color.fail)
        })

        val notificationManager = ContextCompat.getSystemService(
            application,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()

        ok_button.setOnClickListener {
            finish()
        }
    }
}
