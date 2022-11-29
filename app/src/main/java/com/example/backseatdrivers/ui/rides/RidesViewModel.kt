package com.example.backseatdrivers.ui.rides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.backseatdrivers.database.Ride

class RidesViewModel : ViewModel() {

    private val _ride = MutableLiveData<Ride>()
    val ride: LiveData<Ride>
        get() {
            return _ride
        }

    fun getRide(): Ride? {
        return _ride.value
    }

    fun setRide(ride: Ride) {
        println("debug: rides viewmodel update = $ride")
        _ride.value = ride
    }
}