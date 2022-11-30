package com.example.backseatdrivers.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.backseatdrivers.R
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

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()


                snapshot.children.forEach {
                    //get users emails
                    val users= it.child("email").value
                    Log.d("newmessage", users.toString())
                    if(users != null){
                        adapter.add(UserItem(users))
                    }
                }
                
                adapter.setOnItemClickListener { item, view ->
                    val intent = Intent(view.context, ChatLogActivity::class.java )
                    startActivity(intent)
                    finish()
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
