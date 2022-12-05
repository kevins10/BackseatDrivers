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

        val currentNotification = list[position]

        val rideId = currentNotification.ride_id
        val passenger = currentNotification.passenger_id
        val pickupLocation = currentNotification.location
        val date_tv = view.findViewById<TextView>(R.id.na_date)
        val passenger_tv = view.findViewById<TextView>(R.id.na_passenger)
        val location_tv = view.findViewById<TextView>(R.id.na_pickup)
        val acceptBtn = view.findViewById<Button>(R.id.na_accept)
        acceptBtn.setOnClickListener(){
            // add passenger to ride
            CoroutineScope(Dispatchers.Main).launch {
                val passengers = rideId?.let { it1 ->
                    Queries().getPassengerList(
                        it1
                    )
                }
                if (passenger != null) {
                    if (rideId != null) {
                        if (pickupLocation != null) {
                            Queries().addPassengerToRide(rideId, passenger, pickupLocation,
                                passengers as HashMap<String, String>?
                            )
                        }
                    }
                }
            }

            val database : DatabaseReference = Firebase.database.getReference("Requests").child("${currentNotification.request_id}")
            database.removeValue()
        }

        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(passenger.toString())
            val lastName = Queries().getLastName(passenger.toString())
            val date = Queries().getDateFromRideId(currentNotification.ride_id.toString())
            passenger_tv.text = "Request from: $firstName $lastName"
            location_tv.text = "Pickup Location: ${currentNotification.location}"
            date_tv.text = "Date: $date"
        }

        return view
    }
}