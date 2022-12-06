package com.example.backseatdrivers.ui.rides

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.R
import com.example.backseatdrivers.UserViewModel
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentRidesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.collections.ArrayList

class RidesFragment : Fragment() {

    private var _binding: FragmentRidesBinding? = null
    private val binding get() = _binding!!

    private lateinit var arrayList: ArrayList<Ride>
    private lateinit var userViewModel: UserViewModel
    private lateinit var rideDatabase : Query
    private lateinit var userId: String
    private lateinit var filterSpinner: Spinner
    private lateinit var listView : ListView
    private lateinit var ridesAdapter : RidesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRidesBinding.inflate(inflater, container, false)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            userId = user.uid
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterSpinner = view.findViewById(R.id.rides_filter)
        var longTime: Double = System.currentTimeMillis().toDouble()
        rideDatabase = Firebase.database.getReference("Rides").orderByChild("departure_time_milli").startAfter(longTime)
        arrayList = arrayListOf()
        ridesAdapter = RidesAdapter(requireActivity().applicationContext, arrayList)
        listView = view.findViewById(R.id.rides_lv)
        listView.adapter = ridesAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(activity, RideView::class.java)
            intent.putExtra("data", ridesAdapter.getItem(position) as Ride)
            startActivity(intent)
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter).toString()

        update()

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        binding.createRideBtn.setOnClickListener {
            startCreateRideActivity()
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null) {
                    if(parent.getItemAtPosition(position) as String != "None"){
                        Firebase.database.reference.child("Rides").orderByChild("departure_time_milli").startAfter(longTime).get()
                            .addOnSuccessListener { it ->
                                arrayList.clear()
                                for (i in it.children) {
                                    if (parent != null) {
                                        if (i.child("end_address").value.toString().contains(parent.getItemAtPosition(position) as String)) {
                                            val ride = Ride()
                                            ride.ride_id = i.key
                                            ride.departure_time = i.child("departure_time").value.toString()
                                            ride.host_id = i.child("host_id").value.toString()
                                            ride.end_location = i.child("end_location").value.toString()
                                            ride.start_location = i.child("start_location").value.toString()
                                            ride.end_address = i.child("end_address").value.toString()
                                            ride.start_address = i.child("start_address").value.toString()
                                            ride.num_seats = i.child("num_seats").value.toString().toInt()
                                            val passengers = i.child("passengers").value
                                            // check if ride is full
                                            if (passengers != null) {
                                                ride.passengers = passengers as HashMap<String, String>?
                                                if (ride.passengers!!.size == ride.num_seats) {
                                                    ride.is_full = true
                                                }
                                            }

                                            // add ride to array if ride not full
                                            if (ride.is_full != true && ride.host_id != userId) {
                                                arrayList.add(ride)
                                            }
                                        }

                                    }
                                }
                                listView.adapter = ridesAdapter
                            }
                    }

                    else if(parent.getItemAtPosition(position) as String == "None"){
                        update()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }

        }
    }

    private fun startCreateRideActivity() {
        val intent = Intent(activity, CreateRideActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun update(){
        rideDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arrayList.clear()
                filterSpinner.setSelection(0)
                for (i in snapshot.children) {
                    val ride = Ride()
                    ride.ride_id = i.key
                    ride.departure_time = i.child("departure_time").value.toString()
                    ride.host_id = i.child("host_id").value.toString()
                    ride.end_location = i.child("end_location").value.toString()
                    ride.start_location = i.child("start_location").value.toString()
                    ride.end_address = i.child("end_address").value.toString()
                    ride.start_address = i.child("start_address").value.toString()
                    ride.num_seats = i.child("num_seats").value.toString().toInt()
                    ride.departure_time_milli =
                        i.child("departure_time_milli").value.toString().toLong()
                    val passengers = i.child("passengers").value

                    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
                    val dateString = simpleDateFormat.format(ride.departure_time_milli)
                    println("DATE BEING SAVED: ${dateString}")
                    // check if ride is full
                    if (passengers != null) {
                        ride.passengers = passengers as HashMap<String, String>?
                        if (ride.passengers!!.size == ride.num_seats) {
                            ride.is_full = true
                        }
                    }

                    // add ride to array if ride not full
                    if (ride.is_full != true && ride.host_id != userId) {
                        if (passengers == null){
                            arrayList.add(ride)
                        }
                        else if (ride.passengers != null){
                            if (!(ride.passengers!!.containsKey(userId))){
                                arrayList.add(ride)
                            }
                        }

                    }
                }
                listView.adapter = ridesAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }

        })
    }
}
