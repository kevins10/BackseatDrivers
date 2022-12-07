package com.example.backseatdrivers.ui.profile.myrequests

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.ActivityMyRequestsBinding
import com.example.backseatdrivers.ui.home.PassengerRideViewActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MyRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyRequestsBinding

    private lateinit var arrayList: ArrayList<Request>
    private lateinit var database : DatabaseReference
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var listView : ListView
    private lateinit var myRequestsAdapter: MyRequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference.child("Requests")
        arrayList = arrayListOf()
        myRequestsAdapter = MyRequestsAdapter(this, arrayList)
        listView = binding.notifLv
        updateMyRequests()

        if (listView != null) {
            listView.setOnItemClickListener { parent, view, position, id ->
                val request = myRequestsAdapter.getItem(position) as Request
                val rideId = request.ride_id
                val pickupLocation = request.location
                val ridesDatabase = Firebase.database.getReference("Rides").child("$rideId")
                val rideObj = Ride()
                ridesDatabase.get().addOnSuccessListener { it ->
                    rideObj.ride_id = rideId
                    rideObj.departure_time = it.child("departure_time").value.toString()
                    rideObj.host_id = it.child("host_id").value.toString()
                    rideObj.start_location = it.child("start_location").value.toString()
                    rideObj.end_location = it.child("end_location").value.toString()
                    rideObj.start_address = it.child("start_address").value.toString()
                    rideObj.end_address = it.child("end_address").value.toString()
                    rideObj.passengers = it.child("passengers").value as HashMap<String, String>?

                    val intent = Intent(this, PassengerRideViewActivity::class.java)
                    intent.putExtra("data", rideObj)
                    intent.putExtra("isMyRequest", true)
                    intent.putExtra("requestId", "${request.request_id}")
                    intent.putExtra("pickupLocation", pickupLocation)
                    startActivity(intent)
                }
            }
        }
    }

    private fun updateMyRequests() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                for (i in snapshot.children){
                    if(i.child("passenger_id").value == mAuth.currentUser?.uid){
                        val request = Request()
                        request.request_id = i.child("request_id").value.toString()
                        request.ride_id = i.child("ride_id").value.toString()
                        request.location = i.child("location").value.toString()
                        request.host_id = i.child("host_id").value.toString()
                        request.passenger_id = i.child("passenger_id").value.toString()

                        arrayList.add(request)
                    }
                }
                listView.adapter = myRequestsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }
}