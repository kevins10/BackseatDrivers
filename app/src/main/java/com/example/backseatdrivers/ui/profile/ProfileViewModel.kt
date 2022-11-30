package com.example.backseatdrivers.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class ProfileViewModel : ViewModel() {

    var userSnapshot = MutableLiveData<DataSnapshot>()
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    var database : DatabaseReference = Firebase.database.getReference("Users")
    var currentUser : DatabaseReference = database.child(mAuth.currentUser!!.uid)
    lateinit var storageReference: StorageReference
    var profileImage = MutableLiveData<Bitmap>()

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
        getProfileImage()
    }

    private fun getProfileImage() {
        val uid = mAuth.currentUser!!.uid
        storageReference = FirebaseStorage.getInstance().reference.child("Users/$uid.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profileImage.value = bitmap
        }
    }

}