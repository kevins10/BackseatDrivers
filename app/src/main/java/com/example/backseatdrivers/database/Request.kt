package com.example.backseatdrivers.database

data class Request (
    val ride_id: String? = null,
    val passenger_id: String? = null,
    var is_approved: String? = null
)