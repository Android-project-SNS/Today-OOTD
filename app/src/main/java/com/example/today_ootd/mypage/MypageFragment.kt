package com.example.today_ootd.mypage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.today_ootd.LoginActivity
import com.example.today_ootd.MainActivity
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.databinding.FragmentMypageBinding
import com.example.today_ootd.model.ArticleModel
import com.example.today_ootd.model.FollowModel
import com.example.today_ootd.upload.ArticleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentFavoriteBinding
import com.google.firebase.firestore.ktx.toObject

class MypageFragment : Fragment(R.layout.fragment_mypage) {
    private var currentUid : String? = null
    private lateinit var firestore : FirebaseFirestore
    var auth : FirebaseAuth? = null
    val db = Firebase.database
    val userRef = db.getReference("user") // user 정보 레퍼런스
    val postRef = db.getReference("OOTD") // post 정보 레퍼런스
    lateinit var storage: FirebaseStorage
    //private var binding: ItemArticleBinding? = null
    private lateinit var articleDB: DatabaseReference
    private var binding: FragmentMypageBinding? = null
    private val mypageAdapter = MypageAdapter()
    private val articleList = mutableListOf<ArticleModel>()
    private lateinit var followModel : FollowModel

    private val followerUid = mutableListOf<String>()
    private val followingUid = mutableListOf<String>()
    private val followerList = mutableListOf<String>()
    private val followingList = mutableListOf<String>()

    private val listener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(0,articleModel)
            mypageAdapter.submitList(articleList)

        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        binding = FragmentMypageBinding.inflate(inflater, container, false)
        //binding = itemArticleBidning
        storage = Firebase.storage
        firestore = Firebase.firestore

        binding!!.accountRecyclerview.adapter = mypageAdapter

        currentUid = auth!!.currentUser!!.uid

        followerUid.clear()
        followingUid.clear()
        followerList.clear()
        followingList.clear()

        // 로그아웃
        val signoutBtn = binding!!.signoutBtn
        signoutBtn.setOnClickListener { 
            auth?.signOut()
            activity?.finish()
            startActivity(Intent(activity, LoginActivity::class.java))
        }
        
        // 유저 정보 설정 (이름, 이메일, 닉네임)
        userRef.child(currentUid!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                binding!!.myName.text = snapshot.child("name").value.toString()
                binding!!.myEmail.text = snapshot.child("id").value.toString()
                binding!!.myNickname.text = snapshot.child("nickname").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        // 팔로워 수 팔로잉 수 표시
        getFollowerFollowingCount()
        // 게시물 개수 표시
        getPostCount()

        getFollowerFollowing()

        // Inflate the layout for this fragment
        return binding!!.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding
        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        articleList.clear()
        articleDB = Firebase.database.reference.child("OOTD")
        Log.d(ContentValues.TAG, "after addChildEventListener! now size : ${articleList.size}")


        articleDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articleList.clear()
                snapshot.children.forEach {
                    val model = it.getValue(ArticleModel::class.java)
                    model ?: return
                    if (model.sellerId == currentUid)
                        articleList.add(0, model)
                }
                Log.d(ContentValues.TAG, "addListenerForSingleValueEvent is Called!!")
                mypageAdapter.submitList(articleList)
                mypageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        binding!!.accountRecyclerview.layoutManager = GridLayoutManager(context, 3)
        binding!!.accountRecyclerview.adapter = mypageAdapter

        //articleDB.addChildEventListener(listener)
    }

    override fun onResume() {
        super.onResume()

        mypageAdapter.notifyDataSetChanged()
    }
    private fun getFollowerFollowingCount() {
        firestore.collection("users").document(currentUid!!).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener
            if (value.toObject(FollowModel::class.java) != null) {
                followModel = value.toObject(FollowModel::class.java)!!
                binding!!.accountFollowerTextview.text = followModel?.followerCount.toString()
                binding!!.accountFollowingTextview.text = followModel?.followingCount.toString()

                followModel?.followers?.forEach { (key, value) ->
                    followerUid.add(key)
                }

                followModel?.following?.forEach { (key, value) ->
                    followingUid.add(key)
                }
            }
        }
    }

    private fun getPostCount() {
        postRef.addValueEventListener(object : ValueEventListener{
            var postCount = 0
            val uids = mutableListOf<String>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children){
                    val map = child.value as Map<*, *>
                    uids.add(map["sellerId"].toString())
                }
                for (d in uids){
                    if (d == currentUid!!)
                        postCount++
                }
                binding!!.accountPostTextview.text = postCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getFollowerFollowing() {
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
//                val followerList = mutableListOf<String>()
//                val followingList = mutableListOf<String>()
                for (uid in followerUid) {
                    followerList.add(snapshot.child(uid).child("nickname").value.toString())
                }
                for (uid in followingUid) {
                    followingList.add(snapshot.child(uid).child("nickname").value.toString())
                }

                // 팔로워 목록, 팔로잉 목록
                val showFollower = binding!!.accountFollowerTextview
                val showFollowing = binding!!.accountFollowingTextview

                val mainActivity = context as MainActivity

                showFollower.setOnClickListener {
                    val showFriendFragment = ShowFriendFragment(followerList)
                    mainActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, showFriendFragment)
                        .addToBackStack(null)
                        .commit()
                }
                showFollowing.setOnClickListener {
                    val showFriendFragment = ShowFriendFragment(followingList)
                    mainActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, showFriendFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}