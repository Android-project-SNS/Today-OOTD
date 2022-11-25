package com.example.today_ootd.home

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.databinding.ItemArticleBinding
import com.example.today_ootd.model.ArticleModel
import com.example.today_ootd.upload.ArticleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class HomeFragment : Fragment(R.layout.fragment_home) {
    lateinit var storage: FirebaseStorage
    //private var binding: ItemArticleBinding? = null
    private lateinit var articleDB: DatabaseReference
    private var binding: FragmentHomeBinding? = null
    private val articleAdapter = ArticleAdapter()
    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(0,articleModel)
            articleAdapter.submitList(articleList)

        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding
        storage = Firebase.storage
        val storageRef = storage.reference // reference to root

        articleList.clear()
        articleDB = Firebase.database.reference.child("OOTD")
        Log.d(TAG,"after addChildEventListener! now size : ${articleList.size}")

        articleDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articleList.clear()
                snapshot.children.forEach {
                    val model = it.getValue(ArticleModel::class.java)
                    model ?: return
                    articleList.add(0,model)
                }
                Log.d(TAG,"addListenerForSingleValueEvent is Called!!")
                articleAdapter.submitList(articleList)
                articleAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        fragmentHomeBinding.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.itemRecyclerView.adapter = articleAdapter
        articleDB.addChildEventListener(listener)






    }


//    private val mChildListener = object : ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//            // DB에 article이 추가될때마다 동작하는 리스너
//            // article 자체를 객체를 통해서 주고받음
//            val articleModel = snapshot.getValue(ArticleModel::class.java)
//            Log.d(TAG,"addChildEventListener is Called!! now size : ${articleList.size}")
//            articleModel ?: return // null시 반환
//            articleList.add(articleModel)
//            articleAdapter.submitList(articleList)
//            articleAdapter.notifyDataSetChanged() // 추가
//        }
//
//        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//        override fun onChildRemoved(snapshot: DataSnapshot) {}
//        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//        override fun onCancelled(error: DatabaseError) {}
//
//    }
override fun onResume() {
    super.onResume()

    articleAdapter.notifyDataSetChanged()
}
}
