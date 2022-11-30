package com.example.backseatdrivers.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.R
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentNotificationsBinding
import com.example.backseatdrivers.ui.home.HomeAdapter
import com.example.backseatdrivers.ui.rides.RidesAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var requestsDatabase : DatabaseReference
    private lateinit var arrayList: ArrayList<Request>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        notificationsViewModel.update()
        notificationsViewModel.update.observe(viewLifecycleOwner) {
            var rideRequests = notificationsViewModel.getRequestArray()
            var notificationAdapter = NotificationAdapter(requireActivity(), rideRequests)
            var LV = binding.nfLv
            LV.adapter = notificationAdapter
            rideRequests = notificationsViewModel.getRequestArray()
            notificationAdapter.notifyDataSetChanged()
            LV.adapter = notificationAdapter
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}