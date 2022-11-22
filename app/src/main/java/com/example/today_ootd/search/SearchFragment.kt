package com.example.today_ootd.search

import android.content.ContentValues.TAG
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
import com.example.today_ootd.databinding.ItemSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var binding : FragmentSearchBinding? = null

    var auth : FirebaseAuth? = null
    val db = Firebase.database
    val userRef = db.getReference("user") // user 정보 레퍼런스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // 검색 버튼
        val searchButton = binding!!.searchBtn
        searchButton.setOnClickListener {
            // 검색 결과
            existUser()
        }
        // Inflate the layout for this fragment
        return binding!!.root
    }

    // 존재하는 유저인지 아닌지 확인
    private fun existUser() {
        userRef.addValueEventListener(object : ValueEventListener {
            val names = mutableListOf<String>()
            // 검색하고자 하는 닉네임
            val userNickame = binding!!.searchUserName.text.toString()

            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children){
                    val map = child.value as Map<*, *>
                    names.add(map["nickname"].toString())
                }
                // 존재하는 유저라면
                if (names.contains(userNickame)){
                    // 유저 검색 결과
                    val recyclerView = binding!!.userRecyclerView
                    val adapter = UserListAdapter(userNickame)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
                else {
                    println("Not exists")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}



class UserListAdapter(val name : String) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>(){
    inner class UserViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root){
        fun searchResult(){
            binding.name.text = name
            // 팔로우 or 언팔로우
            binding.followBtn.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        Log.d(TAG,"onCreateViewHolder: ")
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.searchResult()
    }
    override fun getItemCount(): Int {
        return 1
    }
}