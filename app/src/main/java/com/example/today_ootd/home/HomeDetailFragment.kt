package com.example.today_ootd.home

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.databinding.FragmentHomeDetailBinding
import com.example.today_ootd.model.ArticleModel
import com.example.today_ootd.upload.ArticleAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class HomeDetailFragment() : Fragment(R.layout.fragment_home_detail) {
    lateinit var storage: FirebaseStorage
    private lateinit var articleDB: DatabaseReference
    private var binding: FragmentHomeDetailBinding? = null
    private val detailAdapter = DetailAdapter()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeDetailBinding = FragmentHomeDetailBinding.bind(view)
        binding = fragmentHomeDetailBinding
        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        articleDB = Firebase.database.reference.child("OOTD")


        articleDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val model = it.getValue(ArticleModel::class.java)
                    model ?: return
                }
                Log.d(ContentValues.TAG, "addListenerForSingleValueEvent is Called!!")

            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

}