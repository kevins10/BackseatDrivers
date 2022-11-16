package com.example.backseatdrivers.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.backseatdrivers.MainActivity
import com.example.backseatdrivers.database.RequestNotification
import com.example.backseatdrivers.database.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationService: Service() {
    private val CHANNEL_ID = "channel id"
    private val NOTIFICATION_ID = 12
    private val REQUEST_CODE = 2

    private lateinit var database: DatabaseReference

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var notificationManager: NotificationManager
//    private lateinit var myBinder: Binder
//    private var msgHandler: Handler? = null

    companion object {
        val STOP_ACTION = "stop notification service"
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()

        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_ACTION)

        database = Firebase.database.getReference("Users")

        broadcastReceiver = myBroadcastReceiver()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }

    private fun newRequestNotification(notification: RequestNotification) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this, CHANNEL_ID
        )

        var passenger: User
        val name = notification.passenger_id?.let {
            database.child(it).get().addOnCompleteListener {
                passenger = it.result.value as User
            }
        }

        notificationBuilder
            .setContentTitle("New Request to join your ride!")
            .setContentText("$")

        val notification = notificationBuilder.build()
        if(Build.VERSION.SDK_INT > 26) {
            val channel = NotificationChannel(CHANNEL_ID, "request channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this, CHANNEL_ID
        )
        notificationBuilder

        val notification = notificationBuilder.build()
        if(Build.VERSION.SDK_INT > 26) {
            val channel = NotificationChannel(CHANNEL_ID, "mapChannel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    inner class myBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            notificationManager.cancel(NOTIFICATION_ID)
            unregisterReceiver(broadcastReceiver)
        }
    }
}