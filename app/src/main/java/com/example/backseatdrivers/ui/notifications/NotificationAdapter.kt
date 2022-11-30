package com.example.backseatdrivers.ui.notifications

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.Ride
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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
        val passenger_tv = view.findViewById<TextView>(R.id.na_passenger)
        val location_tv = view.findViewById<TextView>(R.id.na_pickup)
        val acceptBtn = view.findViewById<Button>(R.id.na_accept)
        acceptBtn.setOnClickListener(){
            var database : DatabaseReference = Firebase.database.getReference("Requests").child("${list[position].request_id}")
            database.removeValue()
        }
        passenger_tv.text = list[position].passenger_id
        location_tv.text = list[position].location
        return view

    }
}