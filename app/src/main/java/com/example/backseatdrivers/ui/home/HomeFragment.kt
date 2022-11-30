package com.example.backseatdrivers.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentHomeBinding
import com.example.backseatdrivers.ui.rides.RideView
import com.google.firebase.database.DataSnapshot

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var upcomingRides = ArrayList<DataSnapshot>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        homeViewModel.update()
        homeViewModel.update.observe(viewLifecycleOwner) {
            upcomingRides = homeViewModel.getSnapshot()
//            for (i in upcomingRides){
//                if (i.child("host_id").value == userViewModel.getUser()?.uid){
//                    println("inside home fragment and host_id is: ${i.child("host_id").value}")
//                    binding.hfTv.text = "${i.child("host_id").value}"
//                }
//            }
        }
        var homeAdapter = HomeAdapter(requireActivity(), upcomingRides)
        var LV = binding.hfLv
        LV.adapter = homeAdapter
        if (LV != null) {
            LV.setOnItemClickListener { parent, view, position, id ->

                var intent = Intent(activity, RideView::class.java)
                intent.putExtra("data", ridesAdapter.getItem(position) as Ride)
                startActivity(intent)

            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}