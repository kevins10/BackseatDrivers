package com.example.backseatdrivers.ui.rides

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.R
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.RequestNotification
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.ActivityRideViewBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class RideView : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapsApiKey: String

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRideViewBinding
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private var markerName: Marker? = null

    private var pickupLocation: LatLng? = null
    private var pickUpAddress: String? = null

    private lateinit var userViewModel: UserViewModel
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRideViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        val intent = intent
        val rideobj = intent.getSerializableExtra("data") as Ride
        binding.rvDate.text = rideobj.departure_time
        binding.rvStart.text = "Start: ${rideobj.start_address}"
        binding.rvDestination.text = "Destination: ${rideobj.end_address}"
        val hostId = rideobj.host_id
        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(hostId.toString())
            val lastName = Queries().getLastName(hostId.toString())
            binding.rvDriver.text = "Driver: $firstName $lastName"

            val user_id = userViewModel.getUser()!!.uid
            userName =
                Queries().getFirstName(user_id).toString() + " " +
                        Queries().getLastName(user_id).toString()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.rv_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initialize Places
        mapsApiKey = com.example.backseatdrivers.BuildConfig.MAPS_API_KEY
        Places.initialize(applicationContext, mapsApiKey)

        locationsAutoComplete(R.id.autoComplete_fragment_pickup_location)

        binding.sendReq.setOnClickListener(){
            onRequest()
        }
        binding.rvCancel.setOnClickListener(){
            finish()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()
        polyLineOptions = PolylineOptions()
        polylines = ArrayList()
        findRoute()
    }

    private fun findRoute() {
        mMap.clear()
        val rideobj = intent.getSerializableExtra("data") as Ride
        val startAddressList = rideobj.start_location!!.split("%20")
        val endAddressList = rideobj.end_location!!.split("%20")

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

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endLocation, 15f))
    }

    private suspend fun fetchDirections(startCoordinates: String, endCoordinates: String, mMap: GoogleMap)
            = suspendCoroutine<Ride> { continuation ->

        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
                "?destination=$startCoordinates" +
                "&origin=$endCoordinates" +
                "&key=$mapsApiKey"

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

    private fun onRequest(){
        if (pickUpAddress != null) {
            val rideobj = intent.getSerializableExtra("data") as Ride
            val request = Request()
            request.ride_id = rideobj.ride_id
            request.request_id = UUID.randomUUID().toString()
            request.host_id = rideobj.host_id
            request.passenger_id = userViewModel.getUser()!!.uid
            request.location = "${pickUpAddress.toString()}%S${pickupLocation?.latitude}%S${pickupLocation?.longitude}"
            val database = Firebase.database.getReference("Requests")

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val currentTime = LocalDateTime.now().format(dateFormatter)
            val newNotification = RequestNotification(
                host_id = rideobj.host_id,
                ride_id = rideobj.ride_id,
                passenger_id = userViewModel.getUser()!!.uid,
                passenger_name = userName,
                post_time = currentTime,
                request_type = "ride_request"
            )
            val notificationsRef = Firebase.database.getReference("Users").child(rideobj.host_id!!).child("notifications")

            //create new request, and also new notification in driver's notification array
            lifecycleScope.launch {
                try {
                    database.child("${request.request_id}").setValue(request)
                }
                catch (e: Exception) { println("debug: could not get ride because $e")}

                try {
                    notificationsRef.child(UUID.randomUUID().toString()).setValue(newNotification)
                }
                catch (e: Exception) { println("debug: error in creating notification = $e")}
            }
            finish()
        } else {
            Toast.makeText(this, "Please enter pickup location", Toast.LENGTH_LONG).show()
        }
    }

    private fun locationsAutoComplete(fragmentId: Int) {

        // Initialize the AutocompleteSupportFragment
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(fragmentId)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
            .setLocationBias(
                RectangularBounds.newInstance(
                    LatLng(49.2827, -123.1207),
                    LatLng(49.3027, -123.1307)
                )
            )
            .setCountries("CA")

        // Set up a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                println("debug4: Place: ${place.address}, ${place.latLng}")
                pickupLocation = place.latLng
                pickUpAddress = "${place.name}\n ${place.address}"

                // add marker
                markerName?.remove()
                markerName = mMap.addMarker(MarkerOptions().position(pickupLocation!!)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation!!, 15f))
            }

            override fun onError(status: Status) {
                println("An error occurred: $status")
            }
        })
    }

}