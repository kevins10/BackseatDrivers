package com.example.backseatdrivers.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.backseatdrivers.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var chatActivityRecyclerView: RecyclerView
    var chatList = ArrayList<Chat>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatActivityRecyclerView = findViewById<RecyclerView>(R.id.chatActivityRecyclerView)
        chatActivityRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        var intent = intent.getSerializableExtra("chatUser") as ChatUser
        var chatUserID = intent.userID

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        reference = FirebaseDatabase.getInstance().getReference("Users").child(chatUserID!!)

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var user = snapshot.child("first_name").value
                var CATV = findViewById<TextView>(R.id.CATV)
                CATV.text = user.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
        var sendBtn = findViewById<Button>(R.id.btnSendMessage)
        var et = findViewById<EditText>(R.id.etMessage)
        sendBtn.setOnClickListener{
            var message:String = et.text.toString()
            sendMessage(firebaseUser!!.uid, chatUserID, message)
            et.setText("")
        }
        readMessage(firebaseUser!!.uid, chatUserID)
    }

    fun sendMessage(senderId: String, receiverId: String, message:String){
        var reference:DatabaseReference = FirebaseDatabase.getInstance().getReference()
        var hashMap:HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)
        reference.child("Chat").push().setValue(hashMap)
    }

    fun readMessage(senderId: String, receiverId: String){
        var databaseReference = FirebaseDatabase.getInstance().getReference("Chat")
        println("SHOULD BE UPDATING ${chatList}")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                println("SHOULD BE UPDATING2 ${chatList}")
                println("this is snapshot? ${snapshot}")
                chatList.clear()
                for (dataSnapshot in snapshot.children){
                    val chat = Chat()
                    chat.message = dataSnapshot.child("message").value.toString()
                    chat.receiverId = dataSnapshot.child("receiverId").value.toString()
                    chat.senderId = dataSnapshot.child("senderId").value.toString()
                    println("NOT INSIDE IF YET ${chat.senderId} == ${chat.receiverId}")
                    if(chat.senderId.equals(senderId) && chat.receiverId.equals(receiverId) || chat.senderId.equals(receiverId) && chat.receiverId.equals(senderId)){
                        println("WE R INSIDE IF YAY ${chat.senderId} == ${chat.receiverId}")
                        chatList.add(chat)
                    }
                }
                var chatAdapter = ChatActivtyAdapter(this@ChatActivity, chatList)
                chatActivityRecyclerView.adapter = chatAdapter
            }
        })

    }
}