package com.example.backseatdrivers.ui.rides

import android.app.Notification
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentRidesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class RidesFragment : Fragment() {

    private var _binding: FragmentRidesBinding? = null

    private val ridesReference: DatabaseReference = Firebase.database.reference.child("Rides")
    private val notificationsReference: DatabaseReference =
        Firebase.database.reference.child("Users").child("Notifications")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRidesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //set listeners
        createNotificationListener()


        return root
    }

    private fun createNotificationListener() {
        val notificationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val notification = dataSnapshot.getValue<ArrayList<Notification>>()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        notificationsReference.addValueEventListener(notificationListener)
    }

    private fun createRidesListener() {
        val notificationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val ride = dataSnapshot.getValue<ArrayList<Ride>>()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        notificationsReference.addValueEventListener(notificationListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}