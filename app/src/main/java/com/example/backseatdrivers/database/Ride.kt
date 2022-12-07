package com.example.backseatdrivers.database

import java.io.Serializable

data class Ride (
    var ride_id: String? = null,
    var host_id: String? = null,
    var driverProfile: DriverProfile? = null,
    var num_seats: Int? = null,
    var is_full:Boolean? = null,
    var start_location: String? = null,
    var end_location: String? = null,
    var start_address: String? = null,
    var end_address: String? = null,
    var departure_time: String? = null,
    var departure_time_milli: Long? = 0,
    var in_progress: String? = "0",

    //fields from map
    var distance: String? = null,
    var duration: String? = null,
    var path: ArrayList<String>? = null, //ArrayList of LatLngs in string format (e.g "24.225213, 53.251522")

    //Array of passenger IDs and pickup location
    var passengers: HashMap<String, String>? = null
) : Serializable
