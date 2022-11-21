package com.example.today_ootd.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.model.ArticleModel
import com.example.today_ootd.upload.ArticleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase



class HomeFragment : Fragment(R.layout.fragment_home) {
//    private lateinit var articleDB: DatabaseReference
//    //private lateinit var userDB: DatabaseReference
//    private lateinit var articleAdapter: ArticleAdapter
//    private val articleList = mutableListOf<ArticleModel>()
//    private val listener = object: ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//
//            val articleModel = snapshot.getValue(ArticleModel::class.java)
//            articleModel ?: return
//
//            articleList.add(articleModel)
//            articleAdapter.submitList(articleList)
//
//        }
//    }
}