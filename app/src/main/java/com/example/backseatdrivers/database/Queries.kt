package com.example.backseatdrivers.database

import android.app.Notification
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class Queries {

    private val database: DatabaseReference = Firebase.database.reference

    val userListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val rides = dataSnapshot.getValue<User>()
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(TAG, "loadUser:onCancelled", databaseError.toException())
        }
    }

    val notificationListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val notification = dataSnapshot.getValue<Notification>()
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    val ridesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val rides = dataSnapshot.getValue<ArrayList<Ride>>()
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(TAG, "loadRides:onCancelled", databaseError.toException())
        }
    }

    fun getUserData(user_id: String): Any? {
        var user = database.child("Users").child(user_id).get().addOnCompleteListener {
            it.result.value as User
        }
        println("user obj: $user")
        return null
    }

    fun getAllRides(): ArrayList<Ride> {
        return database.child("Rides").get() as ArrayList<Ride>
    }

    suspend fun getFirstName(driver: String) = Firebase.database.reference
        .child("Users")
        .child(driver)
        .child("first_name")
        .get()
        .await()
        .value

    suspend fun getLastName(driver: String) = Firebase.database.reference
        .child("Users")
        .child(driver)
        .child("last_name")
        .get()
        .await()
        .value

    suspend fun getDateFromRideId(rideId: String) = Firebase.database.reference
        .child("Rides")
        .child(rideId)
        .child("departure_time")
        .get()
        .await()
        .value
}