package com.example.backseatdrivers.database

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class User(
//    val user_id: String? = null,
    var email: String? = null,
    var password: String? = null,

    var first_name: String? = null,
    var last_name: String? = null,
    var gender: String? = null,
    var age: Int? = null,

    var date_of_birth: String? = null,
    var phone_number: String? = null,
    var home_address: String? = null,

    var driver_profile: DriverProfile? = null,
    var notifications: ArrayList<RequestNotification>? = null

) : Serializable
