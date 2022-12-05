package com.example.backseatdrivers.ui.profile.myrequests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.backseatdrivers.database.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyRequestsViewModel : ViewModel() {

    var userSnapshot = ArrayList<Request>()
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    val update = MutableLiveData<Long>(0L)
    var database : DatabaseReference = Firebase.database.getReference("Requests")

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    fun getRequestArray() : ArrayList<Request>{
        return userSnapshot
    }

    fun update(){
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userSnapshot.clear()
                for (i in snapshot.children){
                    if(i.child("host_id").value == mAuth.currentUser?.uid){
                        var request = Request()
                        request.request_id = i.child("request_id").value.toString()
                        request.ride_id = i.child("ride_id").value.toString()
                        request.location = i.child("location").value.toString()
                        request.host_id = i.child("host_id").value.toString()
                        request.passenger_id = i.child("passenger_id").value.toString()

                        userSnapshot.add(request)
                    }
                }
                println("usersnapshot: ${userSnapshot}")
                update.value = System.currentTimeMillis()
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }

        })
    }
}