package com.example.today_ootd.upload

import android.text.TextUtils.concat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.today_ootd.R
import com.example.today_ootd.databinding.ItemArticleBinding
import com.example.today_ootd.model.ArticleModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ArticleAdapter: ListAdapter<ArticleModel, ArticleAdapter.ViewHolder>(diffUtil){
    private val articleDB: DatabaseReference by lazy{
        Firebase.database.reference.child("OOTD")
    }
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private var currentUid : String? = auth!!.currentUser!!.uid

    inner class ViewHolder (private val binding: ItemArticleBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(articleModel: ArticleModel){
            val format = SimpleDateFormat("MM년dd일")
            val date = Date(articleModel.createdAt)

            binding.titleTextView.text = articleModel.nickname
            //binding.dateTextView.text = format.format(date).toString()
            binding.dateTextView.text = articleModel.style
            binding.heightTextView.text = articleModel.whether
            binding.priceTextView2.text = concat(articleModel.height.toString(), "cm")
            binding.textView.text = articleModel.likeCount.toString()

            if(articleModel.imageUrl.isNotEmpty()){
                Glide.with(binding.thumbnailImageView)
                    .load(articleModel.imageUrl)
                    .into(binding.thumbnailImageView)
            }
        }

        fun setLike(articleModel: ArticleModel){
            var targetArticle = " "
            articleDB.addListenerForSingleValueEvent(object : ValueEventListener{
                var articles = mutableListOf<String>()
                override fun onDataChange(snapshot: DataSnapshot) {
                    articles.clear()
                    for (child in snapshot.children){
                        articles.add(child.key.toString())
                    }
                    val size = articles.size - 1
                    if (size - adapterPosition >= 0)
                        targetArticle = articles[size - adapterPosition]

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            val map = articleModel.like
            var likeCount = articleModel.likeCount
            if (map.containsKey(currentUid)){
                binding.imageView5.setImageResource(R.drawable.ic_favorite_click)
            }
            else {
                binding.imageView5.setImageResource(R.drawable.ic_favoite_empty)
            }
//            binding.textView.text = likeCount.toString()
            // 좋아요 기능
            binding.imageView5.setOnClickListener {
                if (map.containsKey(currentUid!!)){
                    binding.imageView5.setImageResource(R.drawable.ic_favoite_empty)
                    map.remove(currentUid)
                    likeCount--
                    articleDB.child(targetArticle).child("like").setValue(map)
                    articleDB.child(targetArticle).child("likeCount").setValue(likeCount)
                }
                else {
                    binding.imageView5.setImageResource(R.drawable.ic_favorite_click)
                    map[currentUid!!] = true
                    likeCount++
                    articleDB.child(targetArticle).child("like").setValue(map)
                    articleDB.child(targetArticle).child("likeCount").setValue(likeCount)
                }
                binding.textView.text = likeCount.toString()
            }
        }
        fun showDetail(articleModel: ArticleModel){
            binding.thumbnailImageView.setOnClickListener{
                println(articleModel.whether)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemArticleBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
        holder.setLike(currentList[position])
        holder.showDetail(currentList[position])
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