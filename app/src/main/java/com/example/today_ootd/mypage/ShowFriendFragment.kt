package com.example.today_ootd.mypage

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentSearchBinding
import com.example.today_ootd.databinding.FragmentShowFriendBinding
import com.example.today_ootd.databinding.ItemShowfriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ShowFriendFragment : Fragment(R.layout.fragment_show_friend) {
    private var binding : FragmentShowFriendBinding? = null
    private lateinit var firestore : FirebaseFirestore
    private var currentUid : String? = null
    var auth : FirebaseAuth? = null
    val db = Firebase.database
    val userRef = db.getReference("user") // user 정보 레퍼런스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowFriendBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        currentUid = auth!!.currentUser!!.uid

        val list = mutableListOf("hi", "good", "fine")
        val recyclerView = binding!!.friendRecyclerView
        val adapter = FriendListAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Inflate the layout for this fragment
        return binding!!.root
    }

    inner class FriendListAdapter(val friendList : MutableList<String>) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>(){
        inner class FriendViewHolder(private val binding: ItemShowfriendBinding) : RecyclerView.ViewHolder(binding.root){
            fun setFriendList(pos: Int){
                binding.friendNickname.text = friendList[pos]
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
            Log.d(ContentValues.TAG,"onCreateViewHolder: ")
            val binding = ItemShowfriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FriendViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
            holder.setFriendList(position)
        }
        override fun getItemCount(): Int {
            return friendList.size
        }
    }
}