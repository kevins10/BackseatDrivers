package com.example.backseatdrivers.ui.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.backseatdrivers.R
import com.example.backseatdrivers.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        supportActionBar?.title ="Chat Log"

        val userEmail = intent.getStringExtra(NewMessageActivity.USER_EMAIL)
        supportActionBar?.title = userEmail
        setupDummyData()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "attempt to send message")
            performSendMessage()
        }

    }
    class ChatMessage(val id:String ,val text: String ,val fromId:String, val toId: String,
                      val timestamp: Long)

    private fun performSendMessage() {
        //How to send message to firebase
        val text =editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid

        //val fromId = FirebaseDatabase.getInstance().getReference("/Users").parent?.key.toString()
        val userEmail = intent.getStringExtra(NewMessageActivity.USER_EMAIL)
        val userId = intent.getStringExtra(NewMessageActivity.USER_ID).toString()

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