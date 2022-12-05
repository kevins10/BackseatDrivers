package com.example.backseatdrivers.ui.chat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.backseatdrivers.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatActivtyAdapter(private val context: Context, private val chatList: ArrayList<Chat>) :
    RecyclerView.Adapter<ChatActivtyAdapter.ViewHolder>(){

    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT= 1

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MESSAGE_TYPE_RIGHT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
            return ViewHolder(view)
        }
        else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent,false)
            return ViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.tvMessage.text = chat.message

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        if (chatList[position].senderId == firebaseUser!!.uid){
            return  MESSAGE_TYPE_RIGHT
        }
        else{
            return MESSAGE_TYPE_LEFT
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var tvMessage = view.findViewById<TextView>(R.id.tvMessage)
    }


}