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
        val start_tv = view.findViewById<TextView>(R.id.ra_start)
        val dest_tv = view.findViewById<TextView>(R.id.ra_dest)
        val seats_tv = view.findViewById<TextView>(R.id.ra_seats)
        date_tv.text = currentRide.departure_time
        start_tv.text = "Start: ${currentRide.start_address}"
        dest_tv.text = "Destination: ${currentRide.end_address}"
        println("debug: dest: ${currentRide.end_address}")
        var numPassengers = currentRide.passengers?.size
        var numSeats = currentRide.num_seats
        if (numSeats == null) {
            numSeats = 0
        }
        if (numPassengers == null) {
            numPassengers = 0
        }
        val availableSeats = numSeats - numPassengers

        seats_tv.text = "Available seats: $availableSeats/$numSeats"

        return view
    }
}