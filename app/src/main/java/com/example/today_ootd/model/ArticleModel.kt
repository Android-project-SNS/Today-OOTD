package com.example.today_ootd.model

data class ArticleModel (
    val sellerId : String,
    val outer : String,
    val top : String,
    val bottom: String,
    val shoes: String,
    val bag: String,
    val acc: String,
    val createdAt : Long,
    val whether : String,
    val imageUrl : String,
    val nickname: String,
    val style: String,
    val height: Int,
    var like : MutableMap<String, Boolean> = HashMap()
){
   constructor() : this("","","","","","","",0,"","","","",0)
}
