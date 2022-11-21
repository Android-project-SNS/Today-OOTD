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
    private lateinit var articleDB: DatabaseReference
    //private lateinit var userDB: DatabaseReference
    private lateinit var articleAdapter: ArticleAdapter
    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)

        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}


    }

    private var binding: FragmentHomeBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeBidning = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBidning

        binding!!.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        binding!!.itemRecyclerView.adapter = articleAdapter

        articleList.clear()
        articleDB = Firebase.database.reference.child("OOTD")

        fragmentHomeBidning.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBidning.itemRecyclerView.adapter = articleAdapter
        articleDB.addChildEventListener(listener)
    }

//        articleAdapter.submitList(mutableListOf<ArticleModel>().apply {
//            add(ArticleModel("1","맥북 프로16인치",100000,"1,000,000",""))
//            add(ArticleModel("1","갤럭시S22",101010,"800,000",""))
//        })

//    }

    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        articleDB.removeEventListener(listener)
    }
}