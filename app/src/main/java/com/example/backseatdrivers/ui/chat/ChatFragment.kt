package com.example.backseatdrivers.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.backseatdrivers.R
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.backseatdrivers.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatFragment : Fragment() {

//    private var _binding: FragmentChatBinding? = null
    private lateinit var chatRecyclerView: RecyclerView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var userAdapter: ChatAdapter;
//    private val binding get() = _binding!!
    var userlist = ArrayList<ChatUser>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        _binding = FragmentChatBinding.inflate(inflater, container, false)
        var view = inflater.inflate(R.layout.fragment_chat, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userlist = arrayListOf()
        userAdapter = ChatAdapter(requireActivity(), userlist)

        chatRecyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        getUsersList()
        if(userAdapter != null){
            userAdapter.onListItemClick = {it ->
                val intent = Intent(requireActivity(), ChatActivity::class.java)
                intent.putExtra("chatUser", it)
                startActivity(intent)
            }
        }
    }

    fun getUsersList(){
        var firebase: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        var databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userlist.clear()
                for (dataSnapshot:DataSnapshot in snapshot.children){
                    var chatUser = ChatUser()
                    chatUser.userID = dataSnapshot.key.toString()
                    println("THIS IS ID" + chatUser.userID)
                    chatUser.userEmail = dataSnapshot.child("email").value.toString()
                    chatUser.userName = dataSnapshot.child("first_name").value.toString()

                    if (firebase != null) {
                        if (!(chatUser.userID.equals(firebase.uid.toString()))){
                            println("FOR SOME REASON INSIDE IF ${chatUser.userID} same as ${firebase.uid}??")
                            userlist.add(chatUser)
                        }
                    }
                }
                chatRecyclerView.adapter = userAdapter


            }

            override fun onCancelled(error: DatabaseError) {

            }


        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}