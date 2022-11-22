package com.example.today_ootd.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.today_ootd.databinding.ItemArticleBinding
import com.example.today_ootd.databinding.ItemMypageBinding
import com.example.today_ootd.model.ArticleModel
import java.text.SimpleDateFormat
import java.util.*

class MypageAdapter : ListAdapter<ArticleModel, MypageAdapter.ViewHolder>(diffUtil){
    inner class ViewHolder (private val binding: ItemMypageBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(articleModel: ArticleModel){
            val format = SimpleDateFormat("MM년dd일")
            val date = Date(articleModel.createdAt)

            if(articleModel.imageUrl.isNotEmpty()){
                Glide.with(binding.imageView2)
                    .load(articleModel.imageUrl)
                    .into(binding.imageView2)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMypageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        val diffUtil = object  : DiffUtil.ItemCallback<ArticleModel>(){
            override fun areItemsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean {
                return oldItem.createdAt == newItem.createdAt
            }

            override fun areContentsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}