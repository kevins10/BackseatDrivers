package com.example.backseatdrivers.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.Request
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentNotificationsBinding
import com.example.backseatdrivers.ui.rides.RideView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var requestsDatabase : DatabaseReference
    private lateinit var arrayList: ArrayList<Request>
    private lateinit var database : DatabaseReference
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var listView : ListView
    private lateinit var notificationAdapter: NotificationAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        database = Firebase.database.reference.child("Requests")
        arrayList = arrayListOf()
        notificationAdapter = NotificationAdapter(requireActivity(), arrayList)
        listView = binding.nfLv
        updateNotifications()

//        notificationsViewModel.update()
//        notificationsViewModel.update.observe(viewLifecycleOwner) {
//            var rideRequests = notificationsViewModel.getRequestArray()
//            val notificationAdapter = NotificationAdapter(requireActivity(), rideRequests)
//            val listView = binding.nfLv
//            listView.adapter = notificationAdapter
//            rideRequests = notificationsViewModel.getRequestArray()
//            notificationAdapter.notifyDataSetChanged()
//            listView.adapter = notificationAdapter
//        }

        if (listView != null){
            listView.setOnItemClickListener { parent, view, position, id ->
                val intent = Intent(activity, NotificationsActivity::class.java)
                //intent.putExtra("data", ridesAdapter.getItem(position) as Ride)
                startActivity(intent)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateNotifications(){
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                for (i in snapshot.children) {
                    if (i.child("host_id").value == mAuth.currentUser?.uid) {
                        println("we r inside if?!?!")
                        var request = Request()
                        request.request_id = i.child("request_id").value.toString()
                        request.ride_id = i.child("ride_id").value.toString()
                        request.location = i.child("location").value.toString()
                        request.host_id = i.child("host_id").value.toString()
                        request.passenger_id = i.child("passenger_id").value.toString()

                        arrayList.add(request)
                    }
                }
                listView.adapter = notificationAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }
}