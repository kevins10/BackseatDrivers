package com.example.backseatdrivers.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RequestNotification (
    val ride_id: String? = null,
    val passenger_id: String? = null,
    val passenger_name: String? = null,
    val post_time: String? = null,
    val request_type: String? = null
)