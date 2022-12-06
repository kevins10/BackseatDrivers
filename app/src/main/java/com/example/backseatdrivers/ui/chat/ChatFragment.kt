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
import com.example.backseatdrivers.database.Queries
import com.example.backseatdrivers.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class ChatFragment : Fragment() {

//    private var _binding: FragmentChatBinding? = null
    private lateinit var chatRecyclerView: RecyclerView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var userAdapter: ChatAdapter;
    private lateinit var userShow: List<String>
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
        userShow = listOf()


        updateAssociatedUsers()


        if(userAdapter != null){
            userAdapter.onListItemClick = {it ->
                val intent = Intent(requireActivity(), ChatActivity::class.java)
                intent.putExtra("chatUser", it)
                startActivity(intent)
            }
        }
    }


    fun updateAssociatedUsers(){
        var firebase: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        var ridesDatabaseReference = FirebaseDatabase.getInstance().getReference("Rides")
        ridesDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var hs = HashSet<String>()
                var a = listOf<String>()
                for (i in snapshot.children){
                    if (firebase != null) {
                        if (i.child("host_id").value.toString() == firebase.uid){
                            for (x in i.child("passengers").children){
                                hs.add(x.key!!)
                            }
                        } else if (i.hasChild("passengers")){
                            if (firebase != null) {
                                if ((i.child("passengers").value as HashMap<String, String>).containsKey(firebase.uid)){
                                    hs.add(i.child("host_id").value.toString())
                                }
                            }
                        }
                    }
                }
                a = hs.toList()
                for (i in a){
                    runBlocking {
                        var chatUser = ChatUser()
                        chatUser.userName = Queries().getFirstName(i).toString()
                        chatUser.userEmail = Queries().getUserEmail(i).toString()
                        chatUser.userID = i
                        userlist.add(chatUser)
                    }
                }
                chatRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }


        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}