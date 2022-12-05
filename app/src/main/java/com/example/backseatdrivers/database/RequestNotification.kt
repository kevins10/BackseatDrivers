package com.example.backseatdrivers.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RequestNotification (
    val passenger_id: String? = null,
    val request_id: String? = null,
    val message: String? = null,
    val post_time: String? = null,

    val is_read: Boolean? = null
)