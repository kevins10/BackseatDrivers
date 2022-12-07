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
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.RequestNotification
import com.example.backseatdrivers.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class NotificationService: Service() {
    private val CHANNEL_ID = "channel id"
    private val NOTIFICATION_ID = 12
    private val REQUEST_CODE = 2

    private val mAuth = FirebaseAuth.getInstance()

    private lateinit var notificationsRef: DatabaseReference
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationEventListener: ChildEventListener

    private var notificationSnapshot = HashMap<String, RequestNotification>()
//  private lateinit var myBinder: Binder
//  private var msgHandler: Handler? = null

    companion object {
        val STOP_ACTION = "stop notification service"
    }

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_ACTION)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationsRef = Firebase.database.getReference("Users").child(mAuth.currentUser!!.uid).child("notifications")

        //get existing notifications from database on create
//        notificationsRef.get().addOnSuccessListener { snapshot ->
//            for(i in snapshot.children) {
//                val notification = i.getValue<RequestNotification>()
//                if (notification != null) {
//                    notificationSnapshot.set(i.key.toString(), notification)
//                }
//            }
//            println("debug: notification snapshot saved as $notificationSnapshot")
//        }.addOnFailureListener {
//            println("debug: failed to fetch initial notification snapshot")
//        }

        notificationEventListener = object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val requestNotification = snapshot.getValue<RequestNotification>()
                println("debug: new notification has been added,  $snapshot")

                if (requestNotification != null){
                    CoroutineScope(Dispatchers.Main).launch {
                        var fn = requestNotification.host_id?.let { Queries().getFirstName(it) }
                        requestNotification.host_name = fn as String?
                        showRequestNotification(requestNotification)
                    }
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
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("debug: notif service started")
        notificationsRef.addChildEventListener(notificationEventListener) //detached in onDestroy()
        return START_STICKY
    }

    private fun showRequestNotification(notification: RequestNotification) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this, CHANNEL_ID
        )

        if(notification.request_type == "ride_request"){
            notificationBuilder
                .setSmallIcon(com.example.backseatdrivers.R.drawable.ic_baseline_person_pin_16)
                .setContentTitle("New Request!")
                .setContentText("${notification.passenger_name} has requested to join your ride!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }
        else if (notification.request_type == "ride_start"){
            notificationBuilder
                .setSmallIcon(com.example.backseatdrivers.R.drawable.ic_baseline_person_pin_16)
                .setContentTitle("Ride Started")
                .setContentText("${notification.host_name} has started at ${notification.post_time}")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        else if (notification.request_type == "ride_cancel"){
            notificationBuilder
                .setSmallIcon(com.example.backseatdrivers.R.drawable.ic_baseline_person_pin_16)
                .setContentTitle("Ride Canceled")
                .setContentText("${notification.host_name} has canceled a ride")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        else{
            notificationBuilder
                .setSmallIcon(com.example.backseatdrivers.R.drawable.ic_baseline_person_pin_16)
                .setContentTitle("Rider Dropped")
                .setContentText("${notification.passenger_name} has dropped out of your ride")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }


        val notification = notificationBuilder.build()
        if(Build.VERSION.SDK_INT > 26) {
            val channel = NotificationChannel(CHANNEL_ID, "request channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
//        val database : DatabaseReference = Firebase.database.getReference("Users")
//            .child(mAuth.currentUser!!.uid).child("notifications")
//        database.removeValue()
    }


    inner class myBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            notificationManager.cancel(NOTIFICATION_ID)
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onDestroy() {
        notificationsRef.removeEventListener(notificationEventListener)
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}