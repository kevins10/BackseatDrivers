package com.example.backseatdrivers.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.database.Passenger
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentDriverTabBinding
import com.example.backseatdrivers.ui.rides.RidesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class DriverTabFragment : Fragment() {

    private var _binding: FragmentDriverTabBinding? = null
    private val binding get() = _binding!!
    private lateinit var arrayList: ArrayList<Ride>
    private lateinit var rideDatabase : DatabaseReference
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            userId = user.uid
        }

        _binding = FragmentDriverTabBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set on click listener for create a ride button
        arrayList = arrayListOf()
        val ridesAdapter = RidesAdapter(requireActivity().applicationContext, arrayList)
        val LV = binding.hfLv
        LV.adapter = ridesAdapter
        if (LV != null) {
            LV.setOnItemClickListener { parent, view, position, id ->

                // open dialog with ride info
                val intent = Intent(requireActivity(), DriverRideViewActivity::class.java)
                intent.putExtra("data", ridesAdapter.getItem(position) as Ride)
                startActivity(intent)

            }
        }

        rideDatabase = Firebase.database.getReference("Rides")
        rideDatabase.addValueEventListener(object : ValueEventListener {
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

                    // check if ride is posted by current user
                    if (passengers != null) {
                        ride.passengers = passengers as ArrayList<Passenger>
                        if (ride.passengers!!.size == ride.num_seats) {
                            ride.is_full = true
                        }
                    }

                    // add ride to array if hosted by user
                    if (ride.host_id == userId) {
                        arrayList.add(ride)
                    }
                }
                LV.adapter = ridesAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}