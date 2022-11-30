package com.example.backseatdrivers.database

import org.json.JSONObject
import java.io.Serializable
import java.util.UUID

data class Ride (
    var ride_id: UUID? = null,
    var host_id: String? = null,
    var driverProfile: DriverProfile? = null,
    var num_seats: Int? = null,
    var is_full:Boolean? = null,
    var start_location: String? = null,
    var end_location: String? = null,
    var departure_time: String? = null,

    //fields from map
    var distance: String? = null,
    var duration: String? = null,
    var path: ArrayList<String>? = null, //ArrayList of LatLngs in string format (e.g "24.225213, 53.251522")

    //Array of passenger IDs
    var passengers: ArrayList<String>? = null
) : Serializable