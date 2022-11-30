package com.example.backseatdrivers.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG = "CHatLog"
    }
    val adapter= GroupAdapter<ViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerview_chat_log.adapter=adapter
        supportActionBar?.title ="Chat Log"

        val userEmail = intent.getStringExtra(NewMessageActivity.USER_EMAIL)
        supportActionBar?.title = userEmail
        //setupDummyData()
        listenForMessages()
        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "attempt to send message")
            performSendMessage()
        }

    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatFromItem(chatMessage.text))

                    }else{
                        adapter.add(ChatToItem(chatMessage.text))

                    }

                }


            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //TODO("Not yet implemented")
            }
        })

        //TODO("Not yet implemented")
    }

    class ChatMessage(val id:String ,val text: String ,val fromId:String, val toId: String,
                      val timestamp: Long){constructor():this("","","","",-1)}

    private fun performSendMessage() {
        //How to send message to firebase
        val text =editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid

        //val fromId = FirebaseDatabase.getInstance().getReference("/Users").parent?.key.toString()
        val userEmail = intent.getStringExtra(NewMessageActivity.USER_EMAIL)

        val toId = userEmail

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val chatMessage = ChatMessage(reference.key!!,text, fromId!!, toId!!,
            System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "saved our chat message.....:${reference.key}")
            }
        //TODO("Not yet implemented")
    }

    private fun setupDummyData(){
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatFromItem("FROM message"))
        adapter.add(ChatToItem("TO message"))


        recyclerview_chat_log.adapter = adapter
    }
}

class ChatFromItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        //TODO("Not yet implemented")
    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}