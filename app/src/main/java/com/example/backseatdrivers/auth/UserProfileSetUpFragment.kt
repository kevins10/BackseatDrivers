package com.example.backseatdrivers.auth

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.FragmentUserProfileSetUpBinding
import java.util.regex.Matcher
import java.util.regex.Pattern


class UserProfileSetUpFragment : Fragment() {

    private var _binding: FragmentUserProfileSetUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserProfileSetUpBinding.inflate(inflater, container, false)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        var dateOfBirth = ""
        var yearOfBirth = 0
        binding.dobEt.setOnClickListener {
            DatePickerDialog(requireActivity(), { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in EditText
                dateOfBirth = "${(monthOfYear + 1)}/$dayOfMonth/$year"
                yearOfBirth = year
                binding.dobEt.setText(dateOfBirth)
                binding.dobEt.error = null
            }, year, month, day).show()
        }

        binding.nextBtn.setOnClickListener {
            binding.errorMsg.text = null
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val genderId = binding.genderRg.checkedRadioButtonId
            val phone = binding.phoneEt.text.toString()
            val address = binding.addressEt.text.toString()
            val firstName = binding.firstNameEt.text.toString()
            val lastName = binding.lastNameEt.text.toString()
            val dob = binding.dobEt.text.toString()
            val confirmPassword = binding.confirmPasswordEt.text.toString()

            // validate input
            if (firstName.isEmpty()) {
                binding.firstNameEt.error = "First Name is Required"
                return@setOnClickListener
            }
            if (lastName.isEmpty()) {
                binding.lastNameEt.error = "Last Name is Required"
                return@setOnClickListener
            }
            if (genderId == -1) {
                binding.errorMsg.text = "*Gender is Required"
                return@setOnClickListener
            }
            if (dob.isEmpty()) {
                binding.dobEt.error = "Phone Number is Required"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                binding.phoneEt.error = "Phone Number is Required"
                return@setOnClickListener
            } else if (!PhoneNumberUtils.isGlobalPhoneNumber(phone)){
                binding.phoneEt.error = "Not a valid phone number"
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                binding.addressEt.error = "Address is Required"
                return@setOnClickListener
            }
            val domainMatch: Matcher = Pattern.compile(".*@sfu\\.ca").matcher(email)
            if (email.isEmpty()) {
                binding.emailEt.error = "Email is Required"
                return@setOnClickListener
            } else if (!domainMatch.matches()) {
                binding.emailEt.error = "Not a valid email. Please Signup with your SFU email"
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEt.error = "Email address is badly formatted. Please remove any trailing spaces"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordEt.error = "Password is Required"
                return@setOnClickListener
            } else if (password.length < 6) {
                binding.passwordEt.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
               binding.confirmPasswordEt.error = "Confirm Password is Required"
                return@setOnClickListener
            } else if(confirmPassword != password) {
                binding.confirmPasswordEt.error = "Password Does Not Match"
                return@setOnClickListener
            }

            var gender = ""
            val femaleId = binding.femaleRb.id
            val maleId = binding.maleRb.id
            val otherId = binding.otherRb.id
            if (genderId == maleId) {
                gender = "Male"
            } else if (genderId == femaleId) {
                gender = "Female"
            } else if (genderId == otherId) {
                gender = "Other"
            }

            val age = year.minus(yearOfBirth)

            //Create User object and pass into createAccount()
            val user = User(
                email,
                password,
                firstName,
                lastName,
                gender,
                age,
                dateOfBirth,
                phone,
                address
            )

            val bundle = Bundle()
            bundle.putSerializable("user", user)
            val driverProfileSetUpFragment = DriverProfileSetUpFragment()
            driverProfileSetUpFragment.arguments = bundle
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, driverProfileSetUpFragment)
            transaction.commit()
        }

        return binding.root
    }

}