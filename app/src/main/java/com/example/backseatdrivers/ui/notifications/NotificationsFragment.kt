package com.example.backseatdrivers.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.databinding.FragmentNotificationsBinding
import com.google.firebase.database.DatabaseReference

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
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
            val notificationAdapter = NotificationAdapter(requireActivity(), rideRequests)
            val listView = binding.nfLv
            listView.adapter = notificationAdapter
            rideRequests = notificationsViewModel.getRequestArray()
            notificationAdapter.notifyDataSetChanged()
            listView.adapter = notificationAdapter
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}