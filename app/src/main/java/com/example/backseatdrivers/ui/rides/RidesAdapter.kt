package com.example.backseatdrivers.ui.rides

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Ride
import com.google.firebase.database.DataSnapshot

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
        val view = View.inflate(context, R.layout.rides_adapter, null)
        val date_tv = view.findViewById<TextView>(R.id.ra_date)
        val dest_tv = view.findViewById<TextView>(R.id.ra_dest)
        date_tv.text = list[position].departure_time
        dest_tv.text = list[position].end_location
        return view
    }
}