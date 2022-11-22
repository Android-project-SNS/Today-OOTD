package com.example.today_ootd.mypage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.today_ootd.LoginActivity
import com.example.today_ootd.databinding.FragmentMypageBinding
import com.example.today_ootd.model.FollowModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MypageFragment : Fragment() {
    private var uid : String? = null
    private var currentUid : String? = null
    private lateinit var firestore : FirebaseFirestore
    var auth : FirebaseAuth? = null
    val db = Firebase.database
    val userRef = db.getReference("user") // user 정보 레퍼런스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        val binding = FragmentMypageBinding.inflate(inflater, container, false)

        currentUid = auth!!.currentUser!!.uid
        uid = arguments?.getString("destinationUid")

        // 로그아웃
        val signoutBtn = binding.signoutBtn
        signoutBtn.setOnClickListener { 
            auth?.signOut()
            activity?.finish()
            startActivity(Intent(activity, LoginActivity::class.java))
        }
        
        // 유저 정보 설정 (이름, 이메일, 닉네임)
        userRef.child(currentUid!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.myName.text = snapshot.child("name").value.toString()
                binding.myEmail.text = snapshot.child("id").value.toString()
                binding.myNickname.text = snapshot.child("nickname").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        // Inflate the layout for this fragment
        return binding.root
    }

    // 팔로우 기능
    fun requestFollow(){
        var tsDocFollowing = firestore?.collection("users")?.document(currentUid!!)
        firestore?.runTransaction { transaction ->
            var followModel = transaction.get(tsDocFollowing!!).toObject(FollowModel::class.java)
            if (followModel == null){
                followModel = FollowModel()
                followModel!!.followingCount = 1
                followModel!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followModel)
                return@runTransaction
            }

            // 이미 팔로우한 유저라면 (취소)
            if (followModel.following.containsKey(uid)){
                followModel?.followingCount = followModel?.followingCount?.minus(1)!!
                followModel?.followers?.remove(uid)
            }
            // 팔로우 하고 있지 않다면 (팔로우)
            else{
                followModel?.followingCount = followModel?.followingCount?.plus(1)!!
                followModel?.following?.set(uid!!, true)
            }
            transaction.set(tsDocFollowing, followModel)
            return@runTransaction
        }

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followModel = transaction.get(tsDocFollower!!).toObject(FollowModel::class.java)
            if (followModel == null) {
                followModel = FollowModel()
                followModel!!.followerCount = 1
                followModel!!.following[currentUid!!] = true

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
}