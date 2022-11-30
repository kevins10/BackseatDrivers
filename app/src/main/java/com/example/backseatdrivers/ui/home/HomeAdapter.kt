package com.example.backseatdrivers.ui.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Queries
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class HomeAdapter(private val context: Context, private var list: ArrayList<DataSnapshot>): BaseAdapter() {

    private val database: DatabaseReference = Firebase.database.reference

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
        val view = View.inflate(context, R.layout.home_adapter, null)

        val currentItem = list[position]

        val dateTimeTextView = view.findViewById<TextView>(R.id.hf_date_time)
        val driverTextView = view.findViewById<TextView>(R.id.hf_driver)
        val destTextView = view.findViewById<TextView>(R.id.hf_dest)

        val dateTime = currentItem.child("departure_time").value
        var driver = currentItem.child("host_id").value
        val destination = currentItem.child("end_address").value

        CoroutineScope(Dispatchers.Main).launch {
            val firstName = Queries().getFirstName(driver.toString())
            val lastName = Queries().getLastName(driver.toString())
            dateTimeTextView.text = "Leave at: ${dateTime.toString()}"
            driverTextView.text = "Driver: $firstName $lastName"
            destTextView.text = "Desination: ${destination.toString()}"
        }

        return view
    }

}