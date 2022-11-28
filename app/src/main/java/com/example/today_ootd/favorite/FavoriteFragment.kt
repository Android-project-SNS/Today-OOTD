package com.example.today_ootd.favorite

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentFavoriteBinding
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.model.ArticleModel
import com.example.today_ootd.upload.ArticleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {
    lateinit var storage: FirebaseStorage
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    //private var binding: ItemArticleBinding? = null
    private lateinit var articleDB: DatabaseReference
    private var binding: FragmentFavoriteBinding? = null
    private val articleAdapter = ArticleAdapter()
    private val articleList = mutableListOf<ArticleModel>()
    private var currentUid : String? = auth!!.currentUser!!.uid
    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(0, articleModel)
            articleAdapter.submitList(articleList)

        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentFavoriteBinding = FragmentFavoriteBinding.bind(view)
        binding = fragmentFavoriteBinding
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
                    if (model.like.containsKey(currentUid))
                        articleList.add(0, model)
                }
                Log.d(ContentValues.TAG, "addListenerForSingleValueEvent is Called!!")
                articleAdapter.submitList(articleList)
                articleAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        fragmentFavoriteBinding.itemRecyclerView2.layoutManager = LinearLayoutManager(context)
        fragmentFavoriteBinding.itemRecyclerView2.adapter = articleAdapter
        //articleDB.addChildEventListener(listener)
    }

    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }
}