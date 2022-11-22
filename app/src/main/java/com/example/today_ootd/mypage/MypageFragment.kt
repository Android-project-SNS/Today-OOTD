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
    private var currentUid : String? = null
    var auth : FirebaseAuth? = null
    val db = Firebase.database
    val userRef = db.getReference("user") // user 정보 레퍼런스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        val binding = FragmentMypageBinding.inflate(inflater, container, false)

        currentUid = auth!!.currentUser!!.uid

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
}