package com.example.backseatdrivers.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.MainActivity
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.RequestNotification
import com.example.backseatdrivers.database.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class NotificationService: Service() {
    private val CHANNEL_ID = "channel id"
    private val NOTIFICATION_ID = 12
    private val REQUEST_CODE = 2

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationsReference: DatabaseReference
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


        broadcastReceiver = myBroadcastReceiver()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun createNotificationListener() {
        val notificationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val notification = dataSnapshot.getValue<ArrayList<RequestNotification>>()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "debug: notification listener encountered error: ", databaseError.toException())
            }
        }
        notificationsReference.addValueEventListener(notificationListener)
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

        notificationBuilder
            .setContentTitle("New Passenger Request!")
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