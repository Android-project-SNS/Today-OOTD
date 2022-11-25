package com.example.today_ootd.mypage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.today_ootd.LoginActivity
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

    private val listener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
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

        // Inflate the layout for this fragment
        return binding!!.root
    }

    fun getFollowerFollowingCount() {
        firestore.collection("users").document(currentUid!!).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener
            if (value.toObject(FollowModel::class.java) != null) {
                followModel = value.toObject(FollowModel::class.java)!!
                binding!!.accountFollowerTextview.text = followModel?.followerCount.toString()
                binding!!.accountFollowingTextview.text = followModel?.followingCount.toString()
            }
        }
    }

    fun getPostCount() {
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
}