package com.example.backseatdrivers.ui.profile.myrequests

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.databinding.ActivityMyRequestsBinding


class MyRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyRequestsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myRequestsViewModel = ViewModelProvider(this).get(MyRequestsViewModel::class.java)

        myRequestsViewModel.update()
        myRequestsViewModel.update.observe(this) {
            val rideRequests = myRequestsViewModel.getRequestArray()
            val myRequestsAdapter = MyRequestsAdapter(this, rideRequests)
            val listView = binding.notifLv
            listView.adapter = myRequestsAdapter
            myRequestsAdapter.notifyDataSetChanged()
            listView.adapter = myRequestsAdapter
        }
    }
}