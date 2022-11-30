package com.example.backseatdrivers.database

data class Request (
    var request_id: String? = null,
    var ride_id: String? = null,
    var host_id: String? = null,
    var passenger_id: String? = null,
    var location: String? = null
)