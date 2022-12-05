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
    private lateinit var rideDatabase : DatabaseReference
    private lateinit var userViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        create listener for notifications
//        -> each time a new ride is created, the database listener will trigger a notification to let the user know
        //createNotificationListener()

        //set on click listener for create a ride button
        arrayList = arrayListOf()
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

        rideDatabase = Firebase.database.getReference("Rides")
        rideDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                for (i in snapshot.children){
                    var ride : Ride = Ride()
                    ride.ride_id = i.key
                    ride.departure_time = i.child("departure_time").value.toString()
                    ride.host_id = i.child("host_id").value.toString()
                    ride.end_location = i.child("end_location").value.toString()
                    ride.start_location = i.child("start_location").value.toString()
                    ride.end_address = i.child("end_address").value.toString()
                    ride.start_address = i.child("start_address").value.toString()
                    ride.num_seats = i.child("num_seats").value.toString().toInt()
                    val passengers = i.child("passengers").value

                    // check if ride is full
                    if (passengers != null) {
                        ride.passengers = passengers as HashMap<String, String>?
                        if (ride.passengers!!.size == ride.num_seats) {
                            ride.is_full = true
                        }
                    }

                    // add ride to array if ride not full
                    if (ride.is_full != true) {
                        arrayList.add(ride)
                    }
                }
                LV.adapter = ridesAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }


        })
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        view.findViewById<Button>(R.id.createRideBtn).setOnClickListener {
            startCreateRideActivity()
        }
    }

//    private fun createNotificationListener() {
//        val notificationListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val notification = dataSnapshot.getValue<ArrayList<Notification>>()
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        notificationsReference.addValueEventListener(notificationListener)
//    }

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
        intent.putExtra("user", User(first_name = "Bob", last_name = "Marley"))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}