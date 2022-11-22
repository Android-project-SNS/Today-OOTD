package com.example.today_ootd.search

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentSearchBinding
import com.example.today_ootd.databinding.ItemSearchBinding

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var binding : FragmentSearchBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val userName = binding!!.searchUserName
        val searchButton = binding!!.searchBtn
        searchButton.setOnClickListener {

        }
        // Inflate the layout for this fragment
        return binding!!.root
    }
//    inner class UserListAdapter : RecyclerView.Adapter<ViewHolder>(){
//        var userList : ArrayList<String> = arrayListOf()
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            Log.d(TAG,"onCreateViewHolder: ")
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
//
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        }
//
//        override fun getItemCount(): Int {
//            return userList.size
//        }
//
//    }
}