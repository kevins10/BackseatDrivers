package com.example.backseatdrivers.ui.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Ride
import com.google.firebase.database.DataSnapshot

class HomeAdapter(private val context: Context, private var list: ArrayList<DataSnapshot>): BaseAdapter() {
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

        return view

    }
}