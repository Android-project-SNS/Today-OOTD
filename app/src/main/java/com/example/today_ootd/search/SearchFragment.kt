package com.example.today_ootd.search

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentSearchBinding
import com.example.today_ootd.databinding.ItemSearchBinding
import com.example.today_ootd.model.FollowModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var binding : FragmentSearchBinding? = null
    private lateinit var firestore : FirebaseFirestore
    private var currentUid : String? = null
    //private var uid : String? = null
    private var targetUid : String? = null // 팔로우할 대상 uid
    private var myNickname : String? = null // 나의 닉네임
    var auth : FirebaseAuth? = null
    val db = Firebase.database
    val userRef = db.getReference("user") // user 정보 레퍼런스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        currentUid = auth!!.currentUser!!.uid

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
        getMyNickname()
        userRef.addValueEventListener(object : ValueEventListener {
            val nicknames = mutableListOf<String>()
            val uids = mutableListOf<String>()

            // 검색하고자 하는 닉네임
            val userNickname = binding!!.searchUserName.text.toString()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children){
                    val nicknameMap = child.value as Map<*, *>
                    nicknames.add(nicknameMap["nickname"].toString())
                    uids.add(child.key.toString())
                }
                // 본인이 아니고 존재하는 유저라면
                if (nicknames.contains(userNickname) && userNickname != myNickname){
                    val index = nicknames.indexOf(userNickname)
                    println(index)
                    targetUid = uids[index]
                    // 유저 검색 결과
                    val recyclerView = binding!!.userRecyclerView
                    val adapter = UserListAdapter(userNickname)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
                else {
                    Toast.makeText(activity, "존재하지 않는 유저이거나 본인입니다..", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getMyNickname() {
        val myNicknameRef = userRef.child(currentUid!!)
        myNicknameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myNickname = snapshot.child("nickname").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // 팔로우 기능
    fun requestFollow(){
        //println(targetUid)
        var tsDocFollowing = firestore?.collection("users")?.document(currentUid!!)

        firestore?.runTransaction { transaction ->
            var followModel = transaction.get(tsDocFollowing!!).toObject(FollowModel::class.java)
            if (followModel == null){
                followModel = FollowModel()
                followModel!!.followingCount = 1
                followModel!!.following[targetUid!!] = true

                transaction.set(tsDocFollowing, followModel)
                return@runTransaction
            }
            // 이미 팔로우한 유저라면 (취소)
            if (followModel.following.containsKey(targetUid)){
                followModel!!.followingCount = followModel!!.followingCount - 1
                followModel!!.following?.remove(targetUid)
            }
            // 팔로우 하고 있지 않다면 (팔로우)
            else{
                followModel!!.followingCount = followModel!!.followingCount + 1
                followModel?.following?.set(targetUid!!, true)
            }
            transaction.set(tsDocFollowing, followModel)
            return@runTransaction
        }

        var tsDocFollower = firestore?.collection("users")?.document(targetUid!!)
        firestore?.runTransaction { transaction ->
            var followModel = transaction.get(tsDocFollower!!).toObject(FollowModel::class.java)
            if (followModel == null) {
                followModel = FollowModel()
                followModel!!.followerCount = 1
                followModel!!.followers[currentUid!!] = true

                transaction.set(tsDocFollower, followModel!!)
                return@runTransaction
            }
            if (followModel!!.followers.containsKey(currentUid)){
                // 팔로우 하고 있는 경우
                followModel!!.followerCount = followModel!!.followerCount - 1
                followModel!!.followers.remove(currentUid)

            }
            else {
                // 팔로우 하고 있지 않은 경우
                followModel!!.followerCount = followModel!!.followerCount + 1
                followModel!!.followers[currentUid!!] = true
            }
            transaction.set(tsDocFollower, followModel!!)
            return@runTransaction
        }
    }

    inner class UserListAdapter(val name : String) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>(){
        inner class UserViewHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root){
            fun searchResult(){
                binding.name.text = name
                firestore.collection("users").document(currentUid!!).addSnapshotListener { value, error ->
                    if (value == null) return@addSnapshotListener
                    if (value.toObject(FollowModel::class.java) != null) {
                        val followModel = value.toObject(FollowModel::class.java)!!
                        if (followModel.following?.containsKey(targetUid!!) == true)
                            binding.followBtn.text = "팔로우 취소"
                    }
                }
                // 팔로우 or 언팔로우
                binding.followBtn.setOnClickListener {
                    requestFollow()
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
}




