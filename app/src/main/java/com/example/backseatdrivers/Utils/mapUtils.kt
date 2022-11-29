package com.example.backseatdrivers.Utils

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.ui.rides.RidesViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object mapUtils {

     val ridesViewModel = RidesViewModel()

    //draws the route from start to end points, and returns data for the trip
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
//        val requestQueue = Volley.newRequestQueue()
//        requestQueue.add(directionsRequest)
    }
}