package com.example.backseatdrivers.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.backseatdrivers.MainActivity
import com.example.backseatdrivers.database.RequestNotification
import com.example.backseatdrivers.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class NotificationService: Service() {
    private val CHANNEL_ID = "channel id"
    private val NOTIFICATION_ID = 12
    private val REQUEST_CODE = 2

    private val mAuth = FirebaseAuth.getInstance()

    private lateinit var notificationsRef: DatabaseReference
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var notificationManager: NotificationManager
//  private lateinit var myBinder: Binder
//  private var msgHandler: Handler? = null

    companion object {
        val STOP_ACTION = "stop notification service"
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_ACTION)

        notificationsRef = Firebase.database.getReference("Users").child(mAuth.currentUser!!.uid).child("notifications")

        broadcastReceiver = myBroadcastReceiver()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("debug: notif service started")
        newNotificationListener()

        return START_STICKY
    }

    private fun newNotificationListener() {
        notificationsRef.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val requestNotification = snapshot.getValue<RequestNotification>()
                println("debug: new notification has been added = $requestNotification")
                if (requestNotification != null) {
                    showRequestNotification(requestNotification)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showRequestNotification(notification: RequestNotification) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this, CHANNEL_ID
        )

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


    inner class myBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            notificationManager.cancel(NOTIFICATION_ID)
            unregisterReceiver(broadcastReceiver)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}