package com.example.backseatdrivers.ui.rides

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.ActivityCreateRideBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class CreateRideActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateRideBinding

    private lateinit var mapsApiKey: String

    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var geocoder: Geocoder

    private var user: User? = null
    private var ride: Ride? = null

    // host
    private lateinit var database: DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var currentUser : DatabaseReference
    private var hostId: String? = null

    // location
    private var startLocationAddress: String? = null
    private var endLocationAddress: String? = null

    private var startLocationLatLng: LatLng? = null
    private var endLocationLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // get user data
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        database = Firebase.database.getReference("Users")
        if (user != null){
            currentUser = database.child(user.uid)
            hostId = user.uid
            println("debug: current user name: $hostId")
        }

        // initialize Places
        mapsApiKey = com.example.backseatdrivers.BuildConfig.MAPS_API_KEY
        Places.initialize(applicationContext, mapsApiKey)

        locationsAutoComplete(R.id.autoComplete_fragment_start_location)
        locationsAutoComplete(R.id.autoComplete_fragment_end_location)

        binding.findRouteBtn.setOnClickListener {
            findRoute()
        }

        binding.nextBtn.setOnClickListener {
            next()
        }
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
                if (fragmentId == R.id.autoComplete_fragment_start_location) {
                    startLocationLatLng = place.latLng
                    startLocationAddress = place.address
                } else {
                    endLocationLatLng = place.latLng
                    endLocationAddress = place.address
                }

            }

            override fun onError(status: Status) {
                println("An error occurred: $status")
            }
        })
    }

    private fun findRoute() {
        mMap.clear()

        if (startLocationLatLng != null && endLocationLatLng != null) {
            val startLocationStr = startLocationLatLng!!.latitude.toString() + "%20" + startLocationLatLng!!.longitude.toString()
            val endLocationStr = endLocationLatLng!!.latitude.toString() + "%20" +  endLocationLatLng!!.longitude.toString()

            markerOptions.position(startLocationLatLng!!)
            mMap.addMarker(markerOptions)

            markerOptions.position(endLocationLatLng!!)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            mMap.addMarker(markerOptions)

            lifecycleScope.launch {
                try {
                    ride = fetchDirections(startLocationStr, endLocationStr, mMap)
                    println("debug: ride is $ride")
                }
                catch (e: Exception) { println("debug: could not get ride because $e")}
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endLocationLatLng!!, 15f))
        }
        else {
            mMap.clear()
            val vancouver = LatLng(49.2827, -123.1207)
            markerOptions.position(vancouver)
            mMap.addMarker(markerOptions)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(vancouver, 15f))
            Toast.makeText(this, "Something went wrong. Please enter start and end locations", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun fetchDirections(startCoordinates: String, endCoordinates: String, mMap: GoogleMap)
            = suspendCoroutine<Ride> { continuation ->

        println("debug4: startCoord: $startCoordinates, endCoords: $endCoordinates")
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
                "?destination=$endCoordinates" +
                "&origin=$startCoordinates" +
                "&key=$mapsApiKey"

        val path: MutableList<List<LatLng>> = ArrayList()
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener { response ->
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

            continuation.resume(Ride(
                host_id = hostId,
                duration = duration,
                distance = distance,
                is_full = false,
                start_location = startCoordinates,
                end_location = endCoordinates,
                start_address = startLocationAddress,
                end_address = endLocationAddress)
            )
            //set data in viewmodel
        }, Response.ErrorListener {
            continuation.resumeWithException(it)
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    private fun next() {
        val rideDetailsFragment = RideDetailsFragment()

        val bundle = Bundle()
        println("debug: next: ride is $ride")
        bundle.putSerializable("ride", ride)
        rideDetailsFragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.rideDetailsContainer, rideDetailsFragment)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }
}