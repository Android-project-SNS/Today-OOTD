package com.example.today_ootd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.today_ootd.databinding.ActivityMainBinding
import com.example.today_ootd.favorite.FavoriteFragment
import com.example.today_ootd.home.HomeFragment
import com.example.today_ootd.mypage.MypageFragment
import com.example.today_ootd.search.SearchFragment
import com.example.today_ootd.upload.UploadActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    val homeFragment = HomeFragment()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val homeFragment = HomeFragment()
        val favoriteFragment = FavoriteFragment()
        val myPageFragment = MypageFragment()
        val searchFragment = SearchFragment()
        val intent = Intent(this, UploadActivity::class.java)


        //로그인 시에만 업로드 가능하게 하고싶은 경오
//        context?.let {
//                if (auth.currentUser != null) {
//                    val intent = Intent(it, UploadActivity::class.java)
//                    startActivity(intent)
//                } else {
//                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
//                }
        binding!!.toolbarBtnSearch?.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .apply{
                    replace(R.id.fragmentContainer, searchFragment)
                    addToBackStack(null)
                    commit()
                }
        }

        binding!!.bottomNavigationView?.setOnItemSelectedListener { MenuItem ->
            when (MenuItem.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.upload -> startActivity(intent)
                R.id.favorite -> replaceFragment(favoriteFragment)
                R.id.myPage -> replaceFragment(myPageFragment)
            }
            return@setOnItemSelectedListener true
        }
    }

    override fun onStart() {
        //val homeFragment = HomeFragment()
        super.onStart()
        replaceFragment(homeFragment)
    }
    private fun replaceFragment(fragment : Fragment) {
        Log.d("MainActivity","${fragment}")
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer,fragment)
                commit()
            }
    }

    //HomeFragment에 weather location 전달
    fun sendLocation(): FusedLocationProviderClient {
        // 현재 위치의 날씨정보 설정하기
        return LocationServices.getFusedLocationProviderClient(this@MainActivity)
    }

}