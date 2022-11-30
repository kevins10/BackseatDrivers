package com.example.backseatdrivers.ui.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.backseatdrivers.R
import com.example.backseatdrivers.auth.DriverProfileSetUpFragment
import com.example.backseatdrivers.databinding.ActivityEditProfileBinding
import com.example.backseatdrivers.permissions.AppPermissions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var database: DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var currentUser : DatabaseReference
    private lateinit var appPermission: AppPermissions

    private var galleryImageUri: Uri? = null
    private lateinit var  loadImageFromGallery: ActivityResultLauncher<Intent>
    private lateinit var storageReference: StorageReference
    private lateinit var dialog: Dialog
    private var imageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // populate License Type dropdown with values
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item, DriverProfileSetUpFragment.licenseTypes
        )
        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        binding.licenseTypeSpinner.adapter = arrayAdapter

        appPermission = AppPermissions()

        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser
        database = Firebase.database.getReference("Users")
        if (user != null){
            currentUser = database.child(user.uid)
        }

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.update()
        viewModel.userSnapshot.observe(this) {
            val firstName = it.child("first_name").value
            val lastName = it.child("last_name").value
            val phone = it.child("phone_number").value
            val address = it.child("home_address").value
            val driverProfile = it.child("driver_profile")
            val licenseType = driverProfile.child("license_type").value
            val licenseTypeId = licenseType.toString().toInt()
            val vehicleModel = driverProfile.child("vehicle_model").value
            val vehicleColor = driverProfile.child("vehicle_color").value
            val vehicleRules = driverProfile.child("vehicle_rules").value
            binding.firstNameEt.setText("${firstName.toString()}")
            binding.lastNameEt.setText( "${lastName.toString()}")
            binding.phoneEt.setText("${phone.toString()}")
            binding.addressEt.setText("${address.toString()}")
            binding.licenseTypeSpinner.setSelection(licenseTypeId)
            binding.vehicleModelEt.setText("${vehicleModel.toString()}")
            binding.vehicleColorEt.setText("${vehicleColor.toString()}")
            binding.vehicleRulesEt.setText("${vehicleRules.toString()}")
        }

        loadImageFromGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // select and display new profile image from gallery
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null && it.data!!.data != null) {
                    galleryImageUri = it.data!!.data!!
                    binding.profileIv.setImageURI(galleryImageUri)
                }
            }
        }

        viewModel.profileImage.observe(this) {
            binding.profileIv.setImageBitmap(it)
        }

        binding.imgPickImage.setOnClickListener {
            if (appPermission.isStorageOk(this)) {
                selectImageFromGallery()
                imageChanged = true
            } else {
                appPermission.requestStoragePermission(this)
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        loadImageFromGallery.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_save -> {
                onSave()
                return true
            } R.id.menu_cancel -> {
                finish()
                return true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadProfileImage() {
        storageReference = FirebaseStorage.getInstance().getReference("Users/${mAuth.currentUser?.uid}.jpg")
        galleryImageUri?.let {
            storageReference.putFile(it).addOnSuccessListener {
                hideProgressBar()
                Toast.makeText(this, "Successfully uploaded profile image", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Failed to uploaded profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgressBar() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }

    private fun onSave() {
        val firstName = binding.firstNameEt.text.toString()
        val lastName = binding.lastNameEt.text.toString()
        val phone = binding.phoneEt.text.toString()
        val address = binding.addressEt.text.toString()
        val activityType = binding.licenseTypeSpinner.selectedItem.toString()
        val licenseTypeId = DriverProfileSetUpFragment.licenseTypes.indexOf(activityType)
        val vehicleModel = binding.vehicleModelEt.text.toString()
        val vehicleColor = binding.vehicleColorEt.text.toString()
        val vehicleRules = binding.vehicleRulesEt.text.toString()
        currentUser.child("first_name").setValue(firstName)
        currentUser.child("last_name").setValue(lastName)
        currentUser.child("phone_number").setValue(phone)
        currentUser.child("home_address").setValue(address)
        currentUser.child("driver_profile").child("license_type").setValue(licenseTypeId)
        currentUser.child("driver_profile").child("vehicle_model").setValue(vehicleModel)
        currentUser.child("driver_profile").child("vehicle_color").setValue(vehicleColor)
        currentUser.child("driver_profile").child("vehicle_rules").setValue(vehicleRules)

        if (imageChanged) {
            showProgressBar()
            uploadProfileImage()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppPermissions.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery()
            }
        }
    }

}