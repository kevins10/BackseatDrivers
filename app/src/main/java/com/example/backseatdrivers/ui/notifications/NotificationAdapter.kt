package com.example.backseatdrivers.ui.notifications

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.Ride
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationAdapter(private val context: Context, private var list: ArrayList<Request>): BaseAdapter() {
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
        val view = View.inflate(context, R.layout.notification_adapter, null)

        val currentItem = list[position]

        val date_tv = view.findViewById<TextView>(R.id.na_date)
        val passenger_tv = view.findViewById<TextView>(R.id.na_passenger)
        val location_tv = view.findViewById<TextView>(R.id.na_pickup)
        val acceptBtn = view.findViewById<Button>(R.id.na_accept)
        acceptBtn.setOnClickListener(){
            var database : DatabaseReference = Firebase.database.getReference("Requests").child("${currentItem.request_id}")
            database.removeValue()
        }

        val passenger = currentItem.passenger_id
        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(passenger.toString())
            val lastName = Queries().getLastName(passenger.toString())
            val date = Queries().getDateFromRideId(currentItem.ride_id.toString())
            passenger_tv.text = "Request from: $firstName $lastName"
            location_tv.text = "Pickup Location: ${currentItem.location}"
            date_tv.text = "Date: $date"
        }

        return view

    }
}