package com.example.backseatdrivers.database

import java.util.UUID

data class Ride (
    val ride_id: UUID? = null,
    val driverProfile: DriverProfile? = null,
    val num_seats: Int? = null,
    val is_full:Boolean? = null,
    val start_location: String? = null,
    val end_location: String? = null,
    val departure_time: String? = null,

    //Array of passenger IDs
    val passengers: ArrayList<String>? = null
    )