package com.example.backseatdrivers.ui.rides

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.R
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.*
import com.example.backseatdrivers.databinding.ActivityRideViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class RideView : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRideViewBinding
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>

    private var userName: String = ""
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init userviewmodel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        val intent = intent
        var rideobj = intent.getSerializableExtra("data") as Ride
        binding.rvDate.text = rideobj.departure_time
        binding.rvStart.text = "Start: ${rideobj.start_address}"
        binding.rvDestination.text = "Destination: ${rideobj.end_address}"
        val hostId = rideobj.host_id
        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(hostId.toString())
            val lastName = Queries().getLastName(hostId.toString())
            binding.rvDriver.text = "Driver: $firstName $lastName"

            val user_id = userViewModel.getUser()!!.uid
            userName = Queries().getFirstName(user_id).toString() + " " +  Queries().getLastName(user_id)
        }

        binding.sendReq.setOnClickListener(){
            onRequest()
        }
        binding.rvCancel.setOnClickListener(){
            finish()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.rv_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

    fun onRequest(){
        var rideobj = intent.getSerializableExtra("data") as Ride
        var request = Request()
        request.ride_id = rideobj.ride_id
        request.request_id = UUID.randomUUID().toString()
        request.host_id = rideobj.host_id
        request.passenger_id = userViewModel.getUser()!!.uid
        var pickupLocation = binding.pickupLocation.text
        request.location = pickupLocation.toString()
        val requestRefs = Firebase.database.getReference("Requests")

        //Create notification object in driver's user data
        val notificationsRef = Firebase.database.getReference("Users").child("Notifications")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val currentTime = LocalDateTime.now().format(dateFormatter)

        val requestNotification = RequestNotification(
            passenger_id = request.passenger_id,
            request_id = request.request_id,
            post_time = currentTime,
            passenger_name = userName
        )

        println("debug: notification saved as $requestNotification")

        lifecycleScope.launch {
            try {
                requestRefs.child("${request.request_id}").setValue(request)
            } catch (e: Exception) { println("debug: could not get ride because $e")}
            
            try {
                notificationsRef.child(UUID.randomUUID().toString()).setValue(requestNotification)
            } catch (e: Exception) { println("debug: failed to push notification")}
        }
        finish()
    }

}