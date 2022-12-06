package com.example.backseatdrivers

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.ActivityMainBinding
import com.example.backseatdrivers.ui.notifications.NotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Create Listener for user data changes

//        val usersListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                user = dataSnapshot.getValue<User>()
//                if(user != null) {
//                    println("listener object $user")
//                } else {
//                    println("user is null")
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//
//        usersRef = Firebase.database.reference
//            .child("Users")
//            .child(uid.toString())
//        usersRef.addValueEventListener(usersListener)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //start the notification service
        startNotificationService()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_rides, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun startNotificationService() {
        val intent = Intent(this, NotificationService::class.java)
        startService(intent)
    }
}