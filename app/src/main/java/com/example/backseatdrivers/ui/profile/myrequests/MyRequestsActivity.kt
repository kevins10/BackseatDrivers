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
            var rideRequests = myRequestsViewModel.getRequestArray()
            var myRequestsAdapter = MyRequestsAdapter(this, rideRequests)
            var LV = binding.notifLv
            LV.adapter = myRequestsAdapter
            myRequestsAdapter.notifyDataSetChanged()
            LV.adapter = myRequestsAdapter
        }
    }
}