package com.example.backseatdrivers.ui.rides

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentRideDetailsBinding

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class RideDetailsFragment : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!)
    @Suppress("InlinedApi")

    private val ridesViewModel = RidesViewModel()

    private lateinit var ride: Ride

    private var visible: Boolean = false

    private var postRideButton: Button? = null
    private var backButton: Button? = null
    private var fullscreenContent: View? = null

private var _binding: FragmentRideDetailsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      _binding = FragmentRideDetailsBinding.inflate(inflater, container, false)

        val bundle = this.arguments?.getSerializable("ride")
        if(bundle != null) {
            ride = bundle as Ride
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        binding.departureDateEt.setOnClickListener {
            DatePickerDialog(requireActivity(), { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in EditText
                binding.departureDateEt.setText("${(monthOfYear + 1)}/$dayOfMonth/$year")
                binding.departureDateEt.error = null
            }, year, month, day).show()
        }

        val hourOfDay = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        binding.departureTimeEt.setOnClickListener {
            TimePickerDialog(requireActivity(), { view, hour, minute ->
                //Display selected time in EditText
                binding.departureTimeEt.setText("$hour:$minute")
                binding.departureTimeEt.error = null
            } , hourOfDay, minute, false).show()
        }

      return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        fullscreenContent = binding.fullScreenContent
        postRideButton = binding.postRideBtn
        backButton = binding.backBtn

        postRideClickListener()

        backButton!!.setOnClickListener {
            requireActivity().finish()
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    private fun postRideClickListener() {
        val num_passengers = binding.numPassengersEt.text
        val departure_date = binding.departureDateEt.text.toString()
        val departure_time = binding.departureTimeEt.text.toString()

        if(num_passengers.isEmpty()) {
            binding.numPassengersEt.error = "Please enter available seats"
            return
        }

        if(departure_time.isEmpty()) {
            binding.departureTimeEt.error = "Please enter a departure time"
            return
        }
        ride.num_seats = num_passengers.toString().toInt()
        ride.departure_time = departure_time
        ride.departure_time = departure_date + ", " + departure_time

        ridesViewModel.uploadRide(ride)
        Toast.makeText(requireActivity(), "Ride has been posted", Toast.LENGTH_SHORT)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        postRideButton = null
        backButton = null
        fullscreenContent = null
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}