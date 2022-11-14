package com.example.backseatdrivers.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.backseatdrivers.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivitySignUpBinding

    private val TAG = "debug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize Firebase Auth
        auth = Firebase.auth

        binding.signupBtn.setOnClickListener {
            val firstName = binding.firstNameEt.text.toString()
            val lastName = binding.lastNameEt.text.toString()
            val age = binding.ageEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
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

            createAccount(email, password)
        }

    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
//                    val user = auth.currentUser
                    auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                        Toast.makeText(baseContext, "Please verify your Email",
                            Toast.LENGTH_SHORT).show()
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