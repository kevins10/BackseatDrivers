package com.example.backseatdrivers.ui.rides

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.ui.home.HomeAdapter
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RidesAdapter(private val context: Context, private var list: ArrayList<Ride>): BaseAdapter() {
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
        val currentRide = list[position]

        val view = View.inflate(context, R.layout.rides_adapter, null)
        val date_tv = view.findViewById<TextView>(R.id.ra_date)
        val driver_tv = view.findViewById<TextView>(R.id.ra_driver)
        val start_tv = view.findViewById<TextView>(R.id.ra_start)
        val dest_tv = view.findViewById<TextView>(R.id.ra_dest)
        val seats_tv = view.findViewById<TextView>(R.id.ra_seats)
        var numPassengers = currentRide.passengers?.size
        if (numPassengers == null) {
            numPassengers = 0
        }
        val numSeats = currentRide.num_seats
        val driver = currentRide.host_id

        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(driver.toString())
            val lastName = Queries().getLastName(driver.toString())
            driver_tv.text = "Posted By: $firstName $lastName"
        }
        date_tv.text = "Date: ${currentRide.departure_time}"
        start_tv.text = "Start: ${currentRide.start_address}"
        dest_tv.text = "Destination: ${currentRide.end_address}"
        seats_tv.text = "Seats: $numPassengers/$numSeats"

        return view
    }
}