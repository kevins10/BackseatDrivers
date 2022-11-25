package com.example.backseatdrivers.ui.rides

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
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
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text

class CreateRideActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateRideBinding

    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polyLineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var geocoder: Geocoder

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

        userData = intent.getSerializableExtra("user") as User
        println("debug: user object = $userData")
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

        onFindRouteClick()
    }

    private fun onFindRouteClick() {
        val startLocationInput = binding.startLocationInput.editText?.text.toString()
        val endLocationInput = binding.endLocationInput.editText?.text.toString()

        findRouteBtn.setOnClickListener {
            mMap.clear()

            println("debug: input was ${startLocationInput}")

            val startAddressList = geocoder.getFromLocationName(startLocationInput, 1)
            val endAddressList = geocoder.getFromLocationName(endLocationInput, 1)

            println("debug: address = $startAddressList")
            if (startAddressList.size >= 1) {
                val startLocation = LatLng(startAddressList[0].latitude, startAddressList[0].longitude)
                markerOptions.position(startLocation)
                mMap.addMarker(markerOptions)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
            }
            else {
                val startLocation = LatLng(49.2827, -123.1207)
                markerOptions.position(startLocation)
                mMap.addMarker(markerOptions)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
            }
        }
    }
}