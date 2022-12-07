package com.example.backseatdrivers.ui.home

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.RequestNotification
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.ActivityPassengerRideViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PassengerRideViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var rideObj: Ride
    private var pickupLocations: ArrayList<LatLng> = ArrayList()

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassengerRideViewBinding
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var userId: String
    private lateinit var builder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPassengerRideViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            userId = user.uid
        }

        val intent = intent
        val isMyRequest = intent.getBooleanExtra("isMyRequest", false)
        val isViewRequest = intent.getBooleanExtra("isViewRequest", false)
        rideObj = intent.getSerializableExtra("data") as Ride
        displayRideDetails()

        println("debug6: ${rideObj.ride_id}")

        builder = AlertDialog.Builder(this)

        if (isMyRequest) {
            val requestId = intent.getStringExtra("requestId")
            val pickupLocation = intent.getStringExtra("pickupLocation")
            if (pickupLocation != null) {
                val pickupDetails = pickupLocation.split("%S")
                val pickupLatLng = LatLng(pickupDetails[1].toDouble(), pickupDetails[2].toDouble())
                pickupLocations.add(pickupLatLng)
                binding.pickupLocationTv.text = "Requested Pickup Location:\n${pickupDetails[0]}"
            }

            binding.acceptButton.visibility = View.GONE
            binding.removeButton.text = "Cancel Request"
            binding.removeButton.setOnClickListener {

                // alert dialog to confirm drop out of ride
                builder.setTitle("Cancel request for this ride?")
                    .setMessage("Are you sure you want to cancel your request for this ride?")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { dialogInterface, it ->


                        // remove passenger from ride request
                        val database : DatabaseReference = Firebase.database.getReference("Requests").child("$requestId")
                        database.removeValue()

                        // send driver notification
                        finish()
                    }
                    .setNegativeButton("No") { dialogInterface, it ->
                        dialogInterface.cancel()
                    }
                    .show()
            }
        } else if (isViewRequest) {
            val requestId = intent.getStringExtra("requestId")
            val pickupLocation = intent.getStringExtra("pickupLocation")
            val passengerId = intent.getStringExtra("passengerId")
            val rideId = intent.getStringExtra("rideId")
            if (pickupLocation != null) {
                val pickupDetails = pickupLocation.split("%S")
                val pickupLatLng = LatLng(pickupDetails[1].toDouble(), pickupDetails[2].toDouble())
                pickupLocations.add(pickupLatLng)
                binding.pickupLocationTv.text = "Requested Pickup Location:\n${pickupDetails[0]}"
            }

            binding.removeButton.text = "Deny Request"
            binding.removeButton.setOnClickListener {

                // alert dialog to confirm drop out of ride
                builder.setTitle("Deny users Request?")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { dialogInterface, it ->
                        // remove passenger from ride request
                        val database : DatabaseReference = Firebase.database.getReference("Requests").child("$requestId")
                        database.removeValue()

                        // send driver notification
                        finish()
                    }
                    .setNegativeButton("No") { dialogInterface, it ->
                        dialogInterface.cancel()
                    }
                    .show()
            }
            binding.acceptButton.visibility = View.VISIBLE
            binding.acceptButton.setOnClickListener {
                // add passenger to ride
                CoroutineScope(Dispatchers.Main).launch {
                    val passengers = rideId?.let { it1 ->
                        Queries().getPassengerList(
                            it1
                        )
                    }
                    if (passengerId != null) {
                        if (rideId != null) {
                            if (pickupLocation != null) {
                                Queries().addPassengerToRide(rideId, passengerId, pickupLocation,
                                    passengers as HashMap<String, String>?
                                )
                            }
                        }
                    }
                }

                val database : DatabaseReference = Firebase.database.getReference("Requests").child("$requestId")
                database.removeValue()
                finish()
            }

        } else {
            binding.acceptButton.visibility = View.GONE
            binding.removeButton.setOnClickListener {

                // alert dialog to confirm drop out of ride
                builder.setTitle("Drop out of this ride?")
                    .setMessage("Are you sure you want to drop out this ride?")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { dialogInterface, it ->
                        // remove passenger from passengers list
                        println("INSIDE DIALOG")
                        CoroutineScope(Dispatchers.Main).launch {
                            println("INSIDE COROUTINE")
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            val currentTime = LocalDateTime.now().format(dateFormatter)
                            val notificationsRef = Firebase.database.getReference("Users").child(rideObj.host_id!!).child("notifications")
                            var notification = RequestNotification(
                                host_id = rideObj.host_id,
                                ride_id = rideObj.ride_id,
                                passenger_id = userId,
                                passenger_name = Queries().getFirstName(userId).toString(),
                                post_time = currentTime,
                                request_type = "ride_dropped"
                            )
                            try {
                                println("SENT NOTIF")
                                notificationsRef.child(UUID.randomUUID().toString()).setValue(notification)
                            }
                            catch (e: Exception) { println("debug: error in creating notification = $e")
                            }

                        }
                        val database : DatabaseReference = Firebase.database.getReference("Rides")
                            .child("${rideObj.ride_id}")
                            .child("passengers")
                            .child("$userId")
                        database.removeValue()

                        // send driver notification
                        finish()
                    }
                    .setNegativeButton("No") { dialogInterface, it ->
                        dialogInterface.cancel()
                    }
                    .show()
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun displayRideDetails() {
        binding.rvDate.text = rideObj.departure_time
        binding.rvStart.text = "Start: ${rideObj.start_address}"
        binding.rvDestination.text = "Destination: ${rideObj.end_address}"
        val hostId = rideObj.host_id
        val passengers = rideObj.passengers
        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(hostId.toString())
            val lastName = Queries().getLastName(hostId.toString())
            binding.rvDriver.text = "Driver: $firstName $lastName"
        }

        if (passengers != null) {
            val myLinearLayout = binding.passengers

            for (p in passengers) {
                val passengerId = p.key
                val pickupLocation = p.value
                val pickupDetails = pickupLocation.split("%S")
                val pickupAddress = pickupDetails[0]
                val pickupLatLng = LatLng(pickupDetails[1].toDouble(), pickupDetails[2].toDouble())
                pickupLocations.add(pickupLatLng)
                println("debug1: key: $passengerId value: $pickupAddress latLng: $pickupLatLng")

                // create new textviews
                val nameTextView = TextView(this)
                val pickupTextView = TextView(this)

                CoroutineScope(Dispatchers.Main).launch {
                    val firstName = Queries().getFirstName(passengerId)
                    val lastName = Queries().getLastName(passengerId)
                    // set some properties of nameTextView or something
                    nameTextView.text = "Name: $firstName $lastName"
                    pickupTextView.text = "Pickup at: $pickupAddress"
                }

                // add the textview to the linearlayout
                myLinearLayout.addView(nameTextView)
                myLinearLayout.addView(pickupTextView)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()
        polyLineOptions = PolylineOptions()
        polylines = ArrayList()
        findRoute()

        for (location in pickupLocations) {
            markerOptions.position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            mMap.addMarker(markerOptions)
        }
    }

    private fun findRoute() {
        mMap.clear()
        val rideObj = intent.getSerializableExtra("data") as Ride
        val startAddressList = rideObj.start_location!!.split("%20")
        val endAddressList = rideObj.end_location!!.split("%20")

        val startLocation = LatLng(startAddressList[0].toDouble(), startAddressList[1].toDouble())
        val endLocation = LatLng(endAddressList[0].toDouble(), endAddressList[1].toDouble())

        val startLocationStr = startAddressList[0] + "%20" + startAddressList[1]
        val endLocationStr = endAddressList[0] + "%20" +  endAddressList[1]

        markerOptions.position(startLocation)
        mMap.addMarker(markerOptions)

        markerOptions.position(endLocation)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        mMap.addMarker(markerOptions)

        lifecycleScope.launch {
            try {
                fetchDirections(startLocationStr, endLocationStr, mMap)
            }
            catch (e: Exception) { println("debug: could not get ride because $e")}
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
    }

    private suspend fun fetchDirections(startCoordinates: String, endCoordinates: String, mMap: GoogleMap)
            = suspendCoroutine<Ride> { continuation ->

        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
                "?destination=$startCoordinates" +
                "&origin=$endCoordinates" +
                "&key=AIzaSyBcLYz938jT0MGAQGvnio2iV7bWBxrp_bM"

        val path: MutableList<List<LatLng>> = ArrayList()
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            val duration = legs.getJSONObject(0).getJSONObject("duration").get("text").toString()
            val distance = legs.getJSONObject(0).getJSONObject("distance").get("text").toString()

            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for (i in 0 until path.size) {
                mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }

        }, Response.ErrorListener {
            continuation.resumeWithException(it)
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }
}