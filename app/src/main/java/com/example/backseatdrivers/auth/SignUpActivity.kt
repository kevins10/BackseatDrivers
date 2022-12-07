package com.example.backseatdrivers.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.backseatdrivers.databinding.ActivitySignUpBinding


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val userProfileSetUpFragment = UserProfileSetUpFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.fragmentContainer.id, userProfileSetUpFragment)
        transaction.commit()
    }
}