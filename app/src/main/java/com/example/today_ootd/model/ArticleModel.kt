package com.example.today_ootd.model

data class ArticleModel (
    val sellerId : String,
    val outer : String,
    val createdAt : Long,
    val whether : String,
    val imageUrl : String
){
    constructor() : this("","",0,"","")
}
