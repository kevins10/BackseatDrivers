package com.example.backseatdrivers.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_new_message.*

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title ="Select User"
        val adapter = GroupAdapter<ViewHolder>()

        // adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())

        recyclerview_newmessage.adapter=adapter

        fetchUsers()

    }
    companion object{
        val USER_EMAIL = "email"
        val USER_ID="key"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                val userss = snapshot.children.toString()
                Log.d("userValuesssss", userss.toString())

                snapshot.children.forEach {
                    val user = it.getValue().toString()
                    Log.d("userValue", user.toString())


                    val newID=it.key
                    Log.d("UserID", newID.toString())
                    //get users emails
                    val users= it.child("email").value
                    Log.d("newmessage", users.toString())
                    if(users != null){
                        adapter.add(UserItem(users))

                    }
                    adapter.setOnItemClickListener { item, view ->
                        val anotherref = FirebaseAuth.getInstance().uid
                        val userItem = item as UserItem
                        val intent = Intent(view.context, ChatLogActivity::class.java )
                        intent.putExtra(USER_EMAIL,userItem.user.toString())
                        intent.putExtra(USER_ID, newID)
                        //startActivity(intent)
                        //intent.putExtra(USER_ID,anotherref)
                        startActivity(intent)
                        finish()
                    }
                }



                recyclerview_newmessage.adapter= adapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }
}
class UserItem(val user: Any?): Item<ViewHolder>(){


    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_textview_newmessage.text = user.toString()

        //TODO("Not yet implemented")
        //will be called in our list for each user object later on..
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
        //TODO("Not yet implemented")
    }
}