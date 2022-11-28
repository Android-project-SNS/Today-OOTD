package com.example.today_ootd.home

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils.replace
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.today_ootd.MainActivity
import com.example.today_ootd.R
import com.example.today_ootd.adapter.WeatherAdapter
import com.example.today_ootd.component.Common
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.databinding.ItemArticleBinding
import com.example.today_ootd.model.ArticleModel
import com.example.today_ootd.model.ITEM
import com.example.today_ootd.model.WEATHER
import com.example.today_ootd.mypage.ShowFriendFragment
import com.example.today_ootd.network.WeatherObject
import com.example.today_ootd.upload.ArticleAdapter
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment(R.layout.fragment_home) {
    lateinit var storage: FirebaseStorage
    //private var binding: ItemArticleBinding? = null
    private lateinit var articleDB: DatabaseReference
    private var binding: FragmentHomeBinding? = null
    private val articleAdapter = ArticleAdapter()
    private val articleList = mutableListOf<ArticleModel>()

    private var baseDate = "20210510"  // 발표 일자
    private var baseTime = "1400"      // 발표 시각
    private var curPoint : Point? = null    // 현재 위치의 격자 좌표를 저장할 포인트
    private lateinit var weatherAdapter: WeatherAdapter

//    private val listener = object: ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//
//            val articleModel = snapshot.getValue(ArticleModel::class.java)
//            articleModel ?: return
//
//            articleList.add(0,articleModel)
//            articleAdapter.submitList(articleList)
//
//        }
//        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//
//        override fun onChildRemoved(snapshot: DataSnapshot) {}
//
//        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//
//        override fun onCancelled(error: DatabaseError) {}
//    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding
        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        articleList.clear()
        articleDB = Firebase.database.reference.child("OOTD")
        Log.d(TAG,"after addChildEventListener! now size : ${articleList.size}")


        articleDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articleList.clear()
                snapshot.children.forEach {
                    val model = it.getValue(ArticleModel::class.java)
                    model ?: return
                    articleList.add(0,model)
                }
                Log.d(TAG,"addListenerForSingleValueEvent is Called!!")
                articleAdapter.submitList(articleList)
                articleAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        fragmentHomeBinding.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.itemRecyclerView.adapter = articleAdapter
        //articleDB.addChildEventListener(listener)

    val mainActivity = context as MainActivity
    articleAdapter.setOnItemClickListener(object :ArticleAdapter.OnItemClickListener{
        override fun onItemClick(v: View, data: ArticleModel, pos : Int) {
            Log.d("####################detail","###########detail")
            System.out.println("#####detail")
            val homeDetailFragment = HomeDetailFragment()
            mainActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, homeDetailFragment)
                .commit()
        }
    })


        //-------------------날씨 Setting
        weatherAdapter = WeatherAdapter() //초기화

        requestLocation()

        // <새로고침> 버튼 누를 때 위치 정보 & 날씨 정보 다시 가져오기
        binding!!.btnRefresh.setOnClickListener {
            requestLocation()
        }



    }


    // 날씨 가져와서 설정하기
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setWeather(nx : Int, ny : Int) {
        // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
        // 현재 날짜, 시간 정보 가져오기
        val cal = Calendar.getInstance()
        baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시각
        val timeM = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 분
        // API 가져오기 적당하게 변환
        baseTime = Common().getBaseTime(timeH, timeM)
        System.out.println("baseTime : " +baseTime)

        // 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if (timeH == "00" && baseTime == "2330") {
            cal.add(Calendar.DATE, -1).toString() //하루 전
            baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        System.out.println("2. baseDate: " + baseDate + " timeH: " + timeH + " timeM:" + timeM + " baseTime: " + baseTime)

        // 날씨 정보 가져오기
        // (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날싸, 발표 시각, 예보지점 좌표)
        val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", "20221128", "0500", nx, ny)
        //val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", baseDate, baseTime, nx, ny)
        System.out.println("2-1. baseDate: " + baseDate + " baseTime: " + baseTime + " nx:" + nx + " ny: " + ny)
        System.out.println("3. call" + call)

        // 비동기적으로 실행하기
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // 응답 성공 시
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                System.out.println("4. onResponse 실행 중 ")
                if (response.isSuccessful) {
                    // 날씨 정보 가져오기
                    val it: List<ITEM> = response.body()!!.response.body.items.item

                    var rainRatio = ""      // 강수 확률
                    var rainType = ""       // 강수 형태
                    var humidity = ""       // 습도
                    var sky = ""            // 하능 상태
                    var temp = ""           // 기온

                    for (i in 0..59) {
                        when(it[i].category) {
                            "POP" -> rainRatio = it[i].fcstValue    // 강수 확률
                            "PTY" -> rainType = it[i].fcstValue     // 강수 형태
                            "REH" -> humidity = it[i].fcstValue     // 습도
                            "SKY" -> sky = it[i].fcstValue          // 하늘 상태
                            "TMP" -> temp = it[i].fcstValue         // 기온
                            else -> continue
                        }
                    }
                    // 날씨 정보 텍스트뷰에 보이게 하기
                    writeWeather(rainRatio, rainType, humidity, sky, temp)

                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                binding?.tvError?.text = "api fail : " +  t.message.toString() + "\n 다시 시도해주세요."
                binding?.tvError?.visibility = View.VISIBLE
                System.out.println("api fail"+ t.message.toString())
            }
        })
    }

    fun writeWeather(rainRatio : String, rainType : String, humidity : String, sky : String, temp : String){
        System.out.println("rainRatio: " +rainRatio)
        System.out.println("rainType: " +rainType)
        System.out.println("humidity: " +humidity)
        System.out.println("sky: " +sky)
        System.out.println("temp: " +temp)
        binding?.imgWeather?.setImageResource(weatherAdapter.getRainImage(rainType, sky))
        binding?.tvMent?.text = weatherAdapter.getWeatherMent(temp)
        binding?.tvTemp?.text = temp + "° "
        binding?.tvHumidity?.text = humidity + "% "

    }


    // 내 현재 위치의 위경도를 격자 좌표로 변환하여 해당 위치의 날씨정보 설정하기
    @SuppressLint("MissingPermission")
    private fun requestLocation() {

        //getFusedLocationProviderClient가 activity에서만 가능하므로 mainActivity에서 가져와서 사용하기
        val locationClient = (activity as MainActivity).sendLocation()
        System.out.println("1. 장소는 ${locationClient}")


        try {
            // 나의 현재 위치 요청
            val locationRequest = LocationRequest.create()
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 60 * 1000    // 요청 간격(1초)
            }
            val locationCallback = object : LocationCallback() {
                // 요청 결과
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onLocationResult(p0: LocationResult) {
                    p0.let {
                        for (location in it.locations) {
                            // 현재 위치의 위경도를 격자 좌표로 변환
                            System.out.println("1-1. location.latitude는 ${location.latitude} location.longitude는 ${location.longitude}")

                            curPoint = Common().dfsXyConv(location.latitude, location.longitude)

                            System.out.println("1-2. curPoint는 ${curPoint}")

                            // 오늘 날짜 텍스트뷰 설정
                            // nx, ny지점의 날씨 가져와서 설정하기
                            //setWeather(curPoint!!.x, curPoint!!.y)
                            setWeather(55, 127)
                        }
                    }
                }
            }

            // 내 위치 실시간으로 감지
            Looper.myLooper()?.let {
                locationClient.requestLocationUpdates(locationRequest, locationCallback, it)
            }


        } catch (e : SecurityException) {
            e.printStackTrace()
        }
    }


    //    private val mChildListener = object : ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//            // DB에 article이 추가될때마다 동작하는 리스너
//            // article 자체를 객체를 통해서 주고받음
//            val articleModel = snapshot.getValue(ArticleModel::class.java)
//            Log.d(TAG,"addChildEventListener is Called!! now size : ${articleList.size}")
//            articleModel ?: return // null시 반환
//            articleList.add(articleModel)
//            articleAdapter.submitList(articleList)
//            articleAdapter.notifyDataSetChanged() // 추가
//        }
//
//        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//        override fun onChildRemoved(snapshot: DataSnapshot) {}
//        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//        override fun onCancelled(error: DatabaseError) {}
//
//    }
    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }


}
