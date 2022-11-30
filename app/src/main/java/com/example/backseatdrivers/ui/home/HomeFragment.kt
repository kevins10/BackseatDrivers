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
        var homeAdapter = HomeAdapter(requireActivity(), upcomingRides)
        var LV = binding.hfLv
        LV.adapter = homeAdapter
        homeViewModel.update()
        homeViewModel.update.observe(viewLifecycleOwner) {
            upcomingRides = homeViewModel.getSnapshot()
            homeAdapter.notifyDataSetChanged()
            LV.adapter = homeAdapter
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}