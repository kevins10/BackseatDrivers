package com.example.backseatdrivers.ui.rides

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.R
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.ActivityCreateRideBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CreateRideActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateRideBinding

    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var geocoder: Geocoder

    private lateinit var usersRef: DatabaseReference

    private lateinit var findRouteBtn: Button
//    private lateinit var endLocationInput: EditText

    private val ridesViewModel = RidesViewModel()
    private val userViewModel = UserViewModel()
    private var user: User? = null
    private var ride: Ride? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set fragment for more details
//        if (savedInstanceState == null) {
//            supportFragmentManager.commit {
//                setReorderingAllowed(true)
//                add<RideDetailsFragment>(R.id.rideDetailsContainer)
//            }
//        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        nextButtonListener()

        //fetch user data from intent extras, and init ride object
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        userViewModel.getUserObj()?.child(uid.toString())?.get()?.addOnSuccessListener {
            user = it.value as User?
            println("debug: user fetched as = $user")
        }

        //listener for updates to the current ride
        ridesViewModel.ride.observe(this) {viewModelRide ->
            if(viewModelRide != null) {
                ride = viewModelRide
            }
        }

        //initialize UI variables
        findRouteBtn = binding.findRouteBtn
        findRouteClickListener()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()
        polyLineOptions = PolylineOptions()
        polylines = ArrayList()
        geocoder = Geocoder(this)

        // Orient map to Vancouver by default if user does not have a home_address value
        val startPoint = if (user?.home_address != null) {
            val addressList = geocoder.getFromLocationName(user!!.home_address, 1)
            LatLng(addressList[0].latitude, addressList[0].longitude)
        } else {
            LatLng(49.2827, -123.1207)
        }

        markerOptions.position(startPoint)
        mMap.addMarker(markerOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15f))
    }

    private fun findRouteClickListener() {
        findRouteBtn.setOnClickListener {
            mMap.clear()

            val startLocationInput = binding.startLocationInput.text.toString()
            val endLocationInput = binding.endLocationInput.text.toString()

            if(startLocationInput.isEmpty()) {
                binding.startLocationInput.error = "Please enter a start location"
                return@setOnClickListener
            }
            if(endLocationInput.isEmpty()) {
                binding.endLocationInput.error = "Please enter a destination"
                return@setOnClickListener
            }


            val startAddressList = geocoder.getFromLocationName(startLocationInput, 1)
            val endAddressList = geocoder.getFromLocationName(endLocationInput, 1)

            if (startAddressList.size >= 1 && endAddressList.size >= 1) {
                val startLocation = LatLng(startAddressList[0].latitude, startAddressList[0].longitude)
                val endLocation = LatLng(endAddressList[0].latitude, endAddressList[0].longitude)

                val startLocationStr = startAddressList[0].latitude.toString() + "%20" + startAddressList[0].longitude.toString()
                val endLocationStr = endAddressList[0].latitude.toString() + "%20" +  endAddressList[0].longitude.toString()

                markerOptions.position(startLocation)
                mMap.addMarker(markerOptions)

                markerOptions.position(endLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMap.addMarker(markerOptions)

                //Util function for drawing route between two points

                lifecycleScope.launch {
                    try {
                        ride = fetchDirections(startLocationStr, endLocationStr, mMap)
                        println("debug: ride is $ride")
                    }
                    catch (e: Exception) { println("debug: could not get ride because $e")}
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
            }
            else {
                mMap.clear()
                val vancouver = LatLng(49.2827, -123.1207)
                markerOptions.position(vancouver)
                mMap.addMarker(markerOptions)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(vancouver, 15f))
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun fetchDirections(startCoordinates: String, endCoordinates: String, mMap: GoogleMap)
            = suspendCoroutine<Ride> { continuation ->

        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
//                    "?destination=${startCoordinates.latitude}%${startCoordinates.longitude}" +
//                    "&origin=${endCoordinates.latitude}%${endCoordinates.longitude}" +
                "?destination=$startCoordinates" +
                "&origin=$endCoordinates" +
                "&key=AIzaSyBXEhzjnWPdvTk1CclmuYcKtUVSyPUjXL8"

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
                mMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }

            continuation.resume(Ride(
                duration = duration,
                distance = distance)
            )
            //set data in viewmodel
            val ride = Ride(
                duration = duration,
                distance = distance
            )
            ridesViewModel.setRide(ride)
        }, Response.ErrorListener {
            continuation.resumeWithException(it)
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    private fun nextButtonListener() {
        findViewById<Button>(R.id.nextBtn).setOnClickListener {
            val rideDetailsFragment = RideDetailsFragment()

            val bundle = Bundle()
            bundle.putSerializable("ride", ride)
            rideDetailsFragment.arguments = bundle

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.rideDetailsContainer, rideDetailsFragment)
            transaction.setReorderingAllowed(true)
            transaction.commit()
        }
    }

}