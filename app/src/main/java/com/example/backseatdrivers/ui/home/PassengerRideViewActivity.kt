package com.example.backseatdrivers.ui.home

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Queries
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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PassengerRideViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassengerRideViewBinding
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPassengerRideViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            userId = user.uid
        }

        val intent = intent
        var rideobj = intent.getSerializableExtra("data") as Ride
        binding.rvDate.text = rideobj.departure_time
        binding.rvStart.text = "Start: ${rideobj.start_address}"
        binding.rvDestination.text = "Destination: ${rideobj.end_address}"
        val hostId = rideobj.host_id
        val passengers = rideobj.passengers
        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(hostId.toString())
            val lastName = Queries().getLastName(hostId.toString())
            binding.rvDriver.text = "Driver: $firstName $lastName"
        }

        if (passengers != null) {
            val myLinearLayout = binding.pickupLocation

            for (p in passengers) {
                val passengerId = p.key
                val pickup = p.value
                println("debug1: key: $passengerId value: $pickup")

                // create new textviews
                val nameTextView = TextView(this)
                val pickupTextView = TextView(this)

                CoroutineScope(Dispatchers.Main).launch {
                    val firstName = Queries().getFirstName(passengerId)
                    val lastName = Queries().getLastName(passengerId)
                    // set some properties of nameTextView or something
                    nameTextView.text = "Name: $firstName $lastName"
                    pickupTextView.text = "Pickup at: $pickup"
                }

                // add the textview to the linearlayout
                myLinearLayout.addView(nameTextView)
                myLinearLayout.addView(pickupTextView)
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.removeButton.setOnClickListener {
            // remove passenger from passengers list
            val database : DatabaseReference = Firebase.database.getReference("Rides")
                .child("${rideobj.ride_id}")
                .child("passengers")
                .child("$userId")
            database.removeValue()

            // send driver notification
            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()
        polyLineOptions = PolylineOptions()
        polylines = ArrayList()
        // Add a marker in Sydney and move the camera
        findRoute()
    }

    private fun findRoute() {
        mMap.clear()
        var rideobj = intent.getSerializableExtra("data") as Ride
        var startAddressList = rideobj.start_location!!.split("%20")
        var endAddressList = rideobj.end_location!!.split("%20")

        val startLocation = LatLng(startAddressList[0].toDouble(), startAddressList[1].toDouble())
        val endLocation = LatLng(endAddressList[0].toDouble(), endAddressList[1].toDouble())

        val startLocationStr = startAddressList[0] + "%20" + startAddressList[1]
        println("THIS IS START ADRESS ${startLocationStr}")
        val endLocationStr = endAddressList[0] + "%20" +  endAddressList[1]
        println("THIS IS END ADRESS ${endLocationStr}")

        markerOptions.position(startLocation)
        mMap.addMarker(markerOptions)

        markerOptions.position(endLocation)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mMap.addMarker(markerOptions)

        //Util function for drawing route between two points

        lifecycleScope.launch {
            try {
                fetchDirections(startLocationStr, endLocationStr, mMap)

            }
            catch (e: Exception) { println("debug: could not get ride because $e")}
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))


    }

    suspend fun fetchDirections(startCoordinates: String, endCoordinates: String, mMap: GoogleMap)
            = suspendCoroutine<Ride> { continuation ->

        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
//                    "?destination=${startCoordinates.latitude}%${startCoordinates.longitude}" +
//                    "&origin=${endCoordinates.latitude}%${endCoordinates.longitude}" +
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

            //set data in viewmodel

        }, Response.ErrorListener {
            continuation.resumeWithException(it)
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }
}