package com.example.backseatdrivers.database

import org.json.JSONObject
import java.io.Serializable
import java.util.UUID

data class Ride (
    val ride_id: UUID? = null,
    val host_id: String? = null,
    val driverProfile: DriverProfile? = null,
    val num_seats: Int? = null,
    val is_full:Boolean? = null,
    val start_location: String? = null,
    val end_location: String? = null,
    val departure_time: String? = null,

    //fields from map
    var distance: String? = null,
    var duration: String? = null,
    var path: ArrayList<String>? = null, //ArrayList of LatLngs in string format (e.g "24.225213, 53.251522")

    //Array of passenger IDs
    val passengers: ArrayList<String>? = null
    ) : Serializable