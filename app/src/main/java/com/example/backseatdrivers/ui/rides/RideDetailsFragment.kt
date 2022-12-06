package com.example.backseatdrivers.ui.rides

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
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
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.backseatdrivers.database.Ride
import com.example.backseatdrivers.databinding.FragmentRideDetailsBinding
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    private var fullscreenContent: View? = null

    private var _binding: FragmentRideDetailsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideDetailsBinding.inflate(inflater, container, false)

        val bundle = this.arguments?.getSerializable("ride")
        if(bundle != null) {
            ride = bundle as Ride
            println("debug: details: ride is $ride")
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        binding.departureDateEt.setOnClickListener {
            DatePickerDialog(requireActivity(), { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in EditText
                var formatDayOfMonth = dayOfMonth.toString()
                if (dayOfMonth < 10){
                    formatDayOfMonth = "0$dayOfMonth"
                }
                var date = "${(monthOfYear + 1)}-$formatDayOfMonth-$year"
                var localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                binding.departureDateEt.setText("${localDate}")
                binding.departureDateEt.error = null
            }, year, month, day).show()
        }

        val hourOfDay = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        binding.departureTimeEt.setOnClickListener {
            TimePickerDialog(requireActivity(), { view, hour, minute ->
                //Display selected time in EditText
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(
                    Calendar.DAY_OF_MONTH), hour, minute)
                val timeInMillis = calendar.timeInMillis
                val time = formatTimeDate(timeInMillis, "HH:mm:ss")
                binding.departureTimeEt.setText("$time")
                binding.departureTimeEt.error = null
            } , hourOfDay, minute, false).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true
        fullscreenContent = binding.fullScreenContent

        binding.postRideBtn.setOnClickListener {
            postRide()
        }

        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    private fun postRide() {
        val numPassengers = binding.numPassengersEt.text
        val departureDate = binding.departureDateEt.text.toString()
        val departureTime = binding.departureTimeEt.text.toString()

        if(numPassengers.isEmpty()) {
            binding.numPassengersEt.error = "Please enter available seats"
            return
        }

        if(departureTime.isEmpty()) {
            binding.departureTimeEt.error = "Please enter a departure time"
            return
        }
        ride.num_seats = numPassengers.toString().toInt()
        ride.departure_time = departureTime
        ride.departure_time = "$departureDate, $departureTime"
        var localDate = LocalDate.parse(departureDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ride.departure_time_milli = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ridesViewModel.uploadRide(ride)
        Toast.makeText(requireActivity(), "Ride has been posted", Toast.LENGTH_SHORT)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContent = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatTimeDate(milliSeconds: Long, pattern: String?): String? {
        val formatter = SimpleDateFormat(pattern)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

}