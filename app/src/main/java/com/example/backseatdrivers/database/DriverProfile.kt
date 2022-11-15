package com.example.backseatdrivers.database

data class DriverProfile (
    var license_type: Int? = null,
    var vehicle_model: String? = null,
    var vehicle_color: String? = null,
    var vehicle_rules: String? = null
)