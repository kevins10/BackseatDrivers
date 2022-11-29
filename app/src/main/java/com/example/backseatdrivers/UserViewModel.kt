package com.example.backseatdrivers

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserViewModel : ViewModel() {

    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    var database : DatabaseReference = Firebase.database.getReference("Users")

    fun getUser() : FirebaseUser? {
        return mAuth.currentUser
    }

    // myViewModel.getUserObj()?.child("field")?.get()?.addOnSuccessListener { it -> do something }
    // ^^ use that to get data
    fun getUserObj() : DatabaseReference? {
        if(mAuth.currentUser != null){
            return database.child(mAuth.currentUser!!.uid)
        }
        else{
            return null
        }
    }


}