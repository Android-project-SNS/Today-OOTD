package com.example.today_ootd.model

data class FollowModel(
    var followerCount : Int = 0,
    var followers : MutableMap<String, Boolean> = HashMap(),
    
    var followingCount : Int = 0,
    var following : MutableMap<String, Boolean> = HashMap()
)