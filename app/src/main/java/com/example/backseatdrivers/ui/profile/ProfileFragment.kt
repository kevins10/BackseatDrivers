package com.example.backseatdrivers.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.backseatdrivers.R
import com.example.backseatdrivers.auth.LoginActivity
import com.example.backseatdrivers.databinding.FragmentDriverProfileSetUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.backseatdrivers.databinding.FragmentProfileBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    private lateinit var database: DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var currentUser : DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser
        database = Firebase.database.getReference("Users")
        if (user != null){
            currentUser = database.child(user.uid)
            getProfileImage()
        }

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.update()
        viewModel.userSnapshot.observe(viewLifecycleOwner){
            val age = it.child("age").value
            val email = it.child("email").value
            val firstName = it.child("first_name").value
            val lastName = it.child("last_name").value
            binding.firstNameTv.text = "${firstName.toString()}"
            binding.lastNameTv.text = "${lastName.toString()}"
            binding.ageTv.text = "($age)"
        }

        binding.editBtn.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.profileLogoutButton.setOnClickListener(){
            logout(mAuth)
        }
        return binding.root
    }

    private fun logout(mAuth: FirebaseAuth){
        mAuth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        activity?.finish()
        startActivity(intent)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    private fun getProfileImage() {
        val uid = mAuth.currentUser!!.uid
        storageReference = FirebaseStorage.getInstance().reference.child("Users/$uid.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            binding.profileIv.setImageBitmap(bitmap)
        }
    }

}