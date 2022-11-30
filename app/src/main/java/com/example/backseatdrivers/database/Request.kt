package com.example.backseatdrivers.database

data class Request (
    var ride_id: String? = null,
    var passenger_id: String? = null,
    var is_approved: String? = null
)