package com.example.backseatdrivers.ui.profile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.backseatdrivers.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    private lateinit var database: DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var current_user : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser
        database = Firebase.database.getReference("Users")
        if (user != null){
            current_user = database.child(user.uid)
        }

//        if (user != null) {
//            println("gang ${database.child(user.uid).child("age").get().addOnCompleteListener { it ->
//                println("hehe im the best ${it.result.value}")
//            }}")
//        }

        var first_name = view.findViewById<EditText>(R.id.fn_et)
        var last_name = view.findViewById<EditText>(R.id.ln_et)
        var profile_email = view.findViewById<EditText>(R.id.profile_email)
        var saveBtn = view.findViewById<Button>(R.id.profile_save_button)
        var age = view.findViewById<EditText>(R.id.profile_age)
        saveBtn.setOnClickListener(){
            onSave(view)
        }
        current_user.addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val age1 = snapshot.child("age").value
                    val email = snapshot.child("email").value
                    val first_name1 = snapshot.child("first_name").value
                    val last_name1 = snapshot.child("last_name").value
                    first_name.setText("${first_name1.toString()}")
                    last_name.setText("${last_name1.toString()}")
                    profile_email.setText("$email")
                    age.setText("$age1")

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("debug","error reading")
                }

            }
        )

        return view
    }

    fun onSave(view: View){
        val fn = view.findViewById<EditText>(R.id.fn_et).text.toString()
        val ln = view.findViewById<EditText>(R.id.ln_et).text.toString()
        val age = view.findViewById<EditText>(R.id.profile_age).text.toString()
        val profile_email = view.findViewById<EditText>(R.id.profile_email).text.toString()
        current_user.child("first_name").setValue(fn)
        current_user.child("last_name").setValue(ln)
        current_user.child("email").setValue(profile_email)
        current_user.child("age").setValue(age)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

}