package com.example.backseatdrivers.auth

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.backseatdrivers.database.DriverProfile
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.FragmentDriverProfileSetUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class DriverProfileSetUpFragment : Fragment() {

    companion object {
        val licenseTypes: List<String> = arrayListOf(
            "Class 5",
            "Class 7",
            "idk what else"
        )
    }

    private val TAG = "debug"

    private var _binding: FragmentDriverProfileSetUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    //Reference to users collection in database
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverProfileSetUpBinding.inflate(inflater, container, false)

        val licenseTypeSpinner = binding.licenseTypeSpinner

        // populate License Type dropdown with values
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(),
            R.layout.simple_spinner_item, licenseTypes
        )
        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        licenseTypeSpinner.adapter = arrayAdapter

        // initialize Firebase Auth
        auth = Firebase.auth

        //initialize Firebase Realtime Database, with reference to Users collection
        database = Firebase.database.reference.child("Users")

        val bundle = this.arguments
        val user: User = bundle?.getSerializable("user") as User

        binding.signupBtn.setOnClickListener {

            val vehicleModel = binding.vehicleModelEt.text.toString()
            val vehicleColor = binding.vehicleColorEt.text.toString()
            val vehicleRules = binding.vehicleRulesEt.text.toString()

            val licenseType = licenseTypeSpinner.selectedItem.toString()
            val licenseTypeId = licenseTypes.indexOf(licenseType)

            if (vehicleModel.isEmpty()) {
                binding.vehicleModelEt.error = "Vehicle Model is Required"
                return@setOnClickListener
            }
            if (vehicleColor.isEmpty()) {
                binding.vehicleColorEt.error = "Vehicle Color is Required"
                return@setOnClickListener
            }

            val driverProfile = DriverProfile()
            driverProfile.license_type = licenseTypeId
            driverProfile.vehicle_model = vehicleModel
            driverProfile.vehicle_color = vehicleColor
            driverProfile.vehicle_rules = vehicleRules

            user.driver_profile = driverProfile

            createAccount(user)
        }

        return binding.root
    }

    private fun createAccount(user: User) {
        val email = user.email
        val password = user.password
        println("debug1: email: $email, pw: $password")
        if (email != null && password != null) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
//                            Toast.makeText(requireActivity().baseContext, "Please verify your Email",
//                                Toast.LENGTH_SHORT).show()

                            //After auth complete, add user data to Users collection in database
                            database.child(auth.currentUser!!.uid).setValue(user)
                        }
                            ?.addOnFailureListener {
//                                Toast.makeText(requireActivity().baseContext, it.toString(),
//                                    Toast.LENGTH_SHORT).show()
                            }

                        goToVerificationPage(auth.currentUser)
                        activity?.finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                        Toast.makeText(requireActivity().baseContext, "Signup failed.",
//                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun goToVerificationPage(currentUser: FirebaseUser?) {
        val intent = Intent(requireActivity(), VerifySignUpActivity::class.java)
        intent.putExtra("user", currentUser)
        startActivity(intent)
    }

}