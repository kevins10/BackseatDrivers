package com.example.backseatdrivers.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.backseatdrivers.database.User
import com.example.backseatdrivers.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.ref.Reference

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    //Reference to users collection in database
    private lateinit var database: DatabaseReference

    private lateinit var binding: ActivitySignUpBinding

    private val TAG = "debug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize Firebase Auth
        auth = Firebase.auth

        //initialize Firebase Realtime Database, with reference to Users collection
        database = Firebase.database.reference.child("Users")


        binding.signupBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val firstName = binding.firstNameEt.text.toString()
            val lastName = binding.lastNameEt.text.toString()
            val age = binding.ageEt.text.toString()
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
            if (age.isEmpty()) {
                binding.ageEt.error = "Age is Required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.emailEt.error = "Email is Required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordEt.error = "Password is Required"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
               binding.passwordEt.error = "Confirm Password is Required"
                return@setOnClickListener
            }
            if(confirmPassword != password) {
                binding.confirmPasswordEt.error = "Password Does Not Match"
                return@setOnClickListener
            }

            //Create User object and pass into createAccount()
            val user = User(
                email,
                password,
                firstName,
                lastName,
                age
            )
            createAccount(email, password, user)
        }

    }

    private fun createAccount(email: String, password: String, user: User) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
//                    val user = auth.currentUser
                    auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                        Toast.makeText(baseContext, "Please verify your Email",
                            Toast.LENGTH_SHORT).show()

                        //After auth complete, add user data to Users collection in database
                        database.child(auth.currentUser!!.uid).setValue(user)
                    }
                        ?.addOnFailureListener {
                            Toast.makeText(baseContext, it.toString(),
                                Toast.LENGTH_SHORT).show()
                        }

                    updateUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }

    private fun updateUI() {
        val intent = Intent(this, VerifySignUpActivity::class.java)
        startActivity(intent)
    }
}