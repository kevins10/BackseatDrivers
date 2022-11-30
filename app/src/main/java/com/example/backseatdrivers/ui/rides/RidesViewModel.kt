package com.example.backseatdrivers.ui.rides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.backseatdrivers.database.Ride
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class RidesViewModel : ViewModel() {

    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    val database = Firebase.database.getReference("Rides")

    private val _ride = MutableLiveData<Ride>()
    val ride: LiveData<Ride>
        get() {
            return _ride
        }

    fun getRide(): Ride? {
        return _ride.value
    }

    fun setRide(ride: Ride) {
        _ride.value = ride
    }

    fun uploadRide(ride: Ride): Boolean {
        var success = false
        try {
            database.child(UUID.randomUUID().toString()).setValue(ride).addOnCompleteListener {
                success = true
                return@addOnCompleteListener
            }
        } catch (e: FirebaseException) {
            println("debug: database exception = $e")
        }
        return success
    }
}