package com.example.backseatdrivers.ui.chat

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.backseatdrivers.R
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class ChatAdapter(private val context: Context, private val userList: ArrayList<ChatUser>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var onListItemClick: ((ChatUser) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_user,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var user = userList[position]
        holder.userName.text = user.userName
        holder.userEmail.text = user.userEmail
        var storageReference = FirebaseStorage.getInstance().reference.child("Users/${user.userID}.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            holder.picture.setImageBitmap(bitmap)
        }
        holder.layoutUser.setOnClickListener{
            onListItemClick?.invoke(user)
        }
    }


    class ViewHolder(view: View):RecyclerView.ViewHolder(view){

        var userName = view.findViewById<TextView>(R.id.chatUserName)
        var userEmail = view.findViewById<TextView>(R.id.chatUserEmail)
        var layoutUser = view.findViewById<LinearLayout>(R.id.layoutUser)
        var picture = view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.chatUserPicture)

    }

}