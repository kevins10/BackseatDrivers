package com.example.backseatdrivers.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.backseatdrivers.databinding.ActivitySignUpBinding
import com.example.backseatdrivers.databinding.ActivityVerifySignUpBinding

class VerifySignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}