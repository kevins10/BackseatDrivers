package com.example.backseatdrivers.ui.profile.myrequests

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.Request
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyRequestsAdapter(private val context: Context, private var list: ArrayList<Request>): BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.myrequests_adapter, null)

        val currentRequest = list[position]

        val requestId = currentRequest.request_id
        val passenger = currentRequest.passenger_id
        val driver = currentRequest.host_id
        val pickupLocation = currentRequest.location
        val date_tv = view.findViewById<TextView>(R.id.na_date)
        val driver_tv = view.findViewById<TextView>(R.id.na_driver)
        val location_tv = view.findViewById<TextView>(R.id.na_pickup)
        val cancelBtn = view.findViewById<Button>(R.id.na_cancel)
        cancelBtn.setOnClickListener {
            // remove passenger from ride request
            val database : DatabaseReference = Firebase.database.getReference("Requests").child("$requestId")
            database.removeValue()
        }

        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(driver.toString())
            val lastName = Queries().getLastName(driver.toString())
            val date = Queries().getDateFromRideId(currentRequest.ride_id.toString())
            driver_tv.text = "Posted By: $firstName $lastName"
            location_tv.text = "Pickup Location: $pickupLocation"
            date_tv.text = "Date: $date"
        }

        return view
    }
}