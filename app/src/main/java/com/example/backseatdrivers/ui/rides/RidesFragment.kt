package com.example.backseatdrivers.ui.rides

import android.app.Notification
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.R
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.FragmentRidesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class RidesFragment : Fragment() {

    private var _binding: FragmentRidesBinding? = null

    private val ridesReference: DatabaseReference = Firebase.database.reference.child("Rides")
    private val notificationsReference: DatabaseReference =
        Firebase.database.reference.child("Users").child("Notifications")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var arrayList: ArrayList<Ride>
    private lateinit var database: DatabaseReference
    private lateinit var userViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arrayList = arrayListOf(Ride(null,null,null,null,null,"SFU Vancouver","oct 1"),
            Ride(null,null,null,null,null,"SFU Burnaby","sept 5"),
            Ride(null,null,null,null,null,"SFU Surrey","july 1"))
        _binding = FragmentRidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        create listener for notifications
//        -> each time a new ride is created, the database listener will trigger a notification to let the user know
        createNotificationListener()

        //set on click listener for create a ride button
        var ridesAdapter = RidesAdapter(requireActivity().applicationContext, arrayList)
        var LV = view.findViewById<ListView>(R.id.rides_lv)
        LV.adapter = ridesAdapter
        if (LV != null) {
            LV.setOnItemClickListener { parent, view, position, id ->

                var intent = Intent(activity, RideView::class.java)
                intent.putExtra("data", ridesAdapter.getItem(position) as Ride)
                startActivity(intent)

            }
        }
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        view.findViewById<Button>(R.id.createRideBtn).setOnClickListener {
            startCreateRideActivity()
            database = Firebase.database.reference
            var rideobj = Ride(
                ride_id = UUID.randomUUID(), host_id = userViewModel.getUser()?.uid.toString(), null, 4, false,
                "SFU SURREY", "SFU BURNABY", "Oct 21 4 pm", "10km",
                "20 min", null, null)

            database.child("Rides").child("${rideobj.ride_id}").setValue(rideobj)

        }
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

    private fun ridesListener() {
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

    private fun startCreateRideActivity() {
        val intent = Intent(activity, CreateRideActivity::class.java)
        //intent.putExtra("user", User(first_name = "Bob", last_name = "Marley"))
        //startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}