package com.example.backseatdrivers.ui.rides

import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.ActivityCreateRideBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException

class CreateRideActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateRideBinding

    private lateinit var http: OkHttpClient
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var geocoder: Geocoder

    private var directionsObject: String? = null

    private lateinit var findRouteBtn: Button
//    private lateinit var endLocationInput: EditText
    private lateinit var userData: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //init http client and fetch user data from viewmodel
        http = OkHttpClient()
        userData = intent.getSerializableExtra("user") as User
        println("debug: user object = $userData")

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
        val startPoint = if (userData.home_address != null) {
            val addressList = geocoder.getFromLocationName(userData.home_address, 1)
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


                lifecycleScope.launch {
                    getDirections(startLocationStr, endLocationStr)
                }
                println("debug: directions = $directionsObject")

                markerOptions.position(startLocation)
                mMap.addMarker(markerOptions)
                markerOptions.position(endLocation)
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

    private suspend fun getDirections(startCoordinates: String, endCoordinates: String) {

        val request = Request.Builder()
            .url("https://maps.googleapis.com/maps/api/directions/json" +
//                    "?destination=${startCoordinates.latitude}%${startCoordinates.longitude}" +
//                    "&origin=${endCoordinates.latitude}%${endCoordinates.longitude}" +
                    "?destination=$startCoordinates" +
                    "&origin=$endCoordinates" +
                    "&key=AIzaSyDFNc9XgXaO6iDFjP7fhDxX8FQVAmXFY0A")
            .build()

        var coroutine = CoroutineScope(IO).launch {
            http.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        for ((name, value) in response.headers) {
                            println("$name: $value")
                        }

                        directionsObject = response.body!!.string()
                        println("debug: response = $directionsObject")
                    }
                }
            })
        }
        coroutine.join()
    }
}