package com.example.backseatdrivers.database

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class User(
<<<<<<< HEAD
    val user_id: String? = null,
    val email: String? = null,
    val password: String? = null,
=======
//    val user_id: String? = null,
    var email: String? = null,
    var password: String? = null,
>>>>>>> 3c9c91cb95db167399dd9690609e8ec6184f2b5f

    var first_name: String? = null,
    var last_name: String? = null,
    var gender: String? = null,
    var age: Int? = null,

    var date_of_birth: String? = null,
    var phone_number: String? = null,
    var home_address: String? = null,

    var driver_profile: DriverProfile? = null,
    var request_notifications: ArrayList<RequestNotification>? = null
) : Serializable
//class Users(val email:String, val password: String, val first_name: String, val last_name: String,val age: String, val date_of_birth: String, val phone_number: String, val home_address: String){
  //  constructor(): this("","","","","","","","")
//}