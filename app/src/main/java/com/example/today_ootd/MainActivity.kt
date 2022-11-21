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
import com.example.today_ootd.upload.UploadActivity
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val homeFragment = HomeFragment()
        val favoriteFragment = FavoriteFragment()
        val myPageFragment = MypageFragment()
        val intent = Intent(this, UploadActivity::class.java)

        //로그인 시에만 업로드 가능하게 하고싶은 경오
//        context?.let {
//                if (auth.currentUser != null) {
//                    val intent = Intent(it, UploadActivity::class.java)
//                    startActivity(intent)
//                } else {
//                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
//                }


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
    private fun replaceFragment(fragment : Fragment) {
        Log.d("MainActivity","${fragment}")
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentCotainer,fragment)
                commit()
            }
    }

    //HomeFragment에 weather location 전달
    private fun sendLocation(fragment: Fragment){
        val bundle = Bundle(1) // 파라미터의 숫자는 전달하려는 값의 갯수

        // 현재 위치의 날씨정보 설정하기
        val locationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        bundle.putString("location", locationClient.toString())
    }

}