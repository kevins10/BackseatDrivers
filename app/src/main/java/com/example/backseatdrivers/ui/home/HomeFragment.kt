package com.example.backseatdrivers.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
<<<<<<< HEAD

        val textView: TextView = binding.userGreeting
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
=======
        homeViewModel.update()
        val textView: TextView = binding.textHome
        homeViewModel.userSnapshot.observe(viewLifecycleOwner) {
            for (snapshot in it){
                println("fragment now ${snapshot.value}")
            }
>>>>>>> 3c9c91cb95db167399dd9690609e8ec6184f2b5f
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}