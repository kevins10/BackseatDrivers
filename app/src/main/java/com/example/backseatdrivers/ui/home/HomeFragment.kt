package com.example.backseatdrivers.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.backseatdrivers.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fragments: ArrayList<Fragment>
    private lateinit var driverTabFragment: DriverTabFragment
    private lateinit var passengerTabFragment: PassengerTabFragment

    private val tabTitles = arrayOf("AS A DRIVER", "AS A PASSENGER")
    private lateinit var tabLayout: TabLayout
    private lateinit var tabConfigStrategy: TabLayoutMediator.TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    private lateinit var viewPager: ViewPager2
    private lateinit var fragmentStateAdapter: FragmentStateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        driverTabFragment = DriverTabFragment()
        passengerTabFragment = PassengerTabFragment()

        fragments = ArrayList()
        fragments.add(driverTabFragment)
        fragments.add(passengerTabFragment)

        tabLayout = binding.tab
        viewPager = binding.viewPager

        fragmentStateAdapter = FragmentStateAdapter(requireActivity(), fragments)
        viewPager.adapter = fragmentStateAdapter

        // link tabLayout with viewPager to get corresponding view when selected/scrolled
        tabConfigStrategy = TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
        }

        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager, tabConfigStrategy)
        tabLayoutMediator.attach()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }

}