package com.example.backseatdrivers.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    val email: String? = null,
    val password: String? = null,

    val first_name: String? = null,
    val last_name: String? = null,
    val age: String? = null,

    /*
    TODO: add fields to signup

    var date_of_birth: String,
    var phone_number: String,
    var home_address: String,

    var driver_profile: DriverProfile
    */
)
