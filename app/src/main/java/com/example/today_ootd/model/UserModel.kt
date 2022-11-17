package com.example.today_ootd.model

data class UserModel (
    val name : String,
    val nickname : String,
    val height : Int
){
    constructor() : this("", "", 0)
}