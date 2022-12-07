package com.example.backseatdrivers.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.backseatdrivers.databinding.ActivitySignUpBinding
import com.example.backseatdrivers.databinding.ActivityVerifySignUpBinding
import com.google.firebase.auth.FirebaseUser

class VerifySignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val user = intent.getParcelableExtra<FirebaseUser>("user")
        val userEmail = user?.email

        binding.verifyEmailActionsTv.text = "An email has been sent to:\n $userEmail\n " +
                "Please follow the instructions in the verification email to finish creating your account."

        binding.continueBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.resendEmail.setOnClickListener {
            user?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(baseContext, "Verification email sent!",
                    Toast.LENGTH_SHORT).show()
            }
                ?.addOnFailureListener {
                    Toast.makeText(baseContext, it.toString(),
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

}