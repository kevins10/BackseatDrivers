package com.example.backseatdrivers.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.backseatdrivers.database.Ride
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {

    var userSnapshot = ArrayList<DataSnapshot>()
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    var database : DatabaseReference = Firebase.database.getReference("Rides")
//    var userRides : DatabaseReference = database.child("Rides")
    var upcomingRides: ArrayList<Ride> = arrayListOf()
    val update = MutableLiveData<Long>(0L)

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    fun getSnapshot(): ArrayList<DataSnapshot>{
        return userSnapshot
    }

    val text: LiveData<String> = _text

    fun update(){
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //println("this is kinda working ${snapshot}")
                userSnapshot.clear()
                for (dataSnapshot in snapshot.children){
                    println("this is kinda working ${dataSnapshot.child("host_id").value}")
                    if (dataSnapshot.child("host_id").value == mAuth.currentUser?.uid){
                        println("adding")
                        userSnapshot.add(dataSnapshot)
                    }
                }
                update.value = System.currentTimeMillis()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}