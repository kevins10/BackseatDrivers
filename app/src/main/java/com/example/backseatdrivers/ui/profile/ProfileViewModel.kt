package com.example.backseatdrivers.ui.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileViewModel : ViewModel() {

    var userSnapshot = MutableLiveData<DataSnapshot>()
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    var database : DatabaseReference = Firebase.database.getReference("Users")
    var currentUser : DatabaseReference = database.child(mAuth.currentUser!!.uid)


    fun update() {
        currentUser.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userSnapshot.value = snapshot
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("debug", "error reading")
                }
            }
        )

    }

}