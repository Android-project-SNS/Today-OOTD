package com.example.today_ootd.home

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Point
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.example.today_ootd.R
import com.example.today_ootd.adapter.WeatherAdapter
import com.example.today_ootd.component.Common
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.model.ITEM
import com.example.today_ootd.model.WEATHER
import com.example.today_ootd.model.WeatherModel
import com.example.today_ootd.network.WeatherObject
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var baseDate = "20210510"  // 발표 일자
    private var baseTime = "1400"      // 발표 시각
    private var curPoint : Point? = null    // 현재 위치의 격자 좌표를 저장할 포인트

    lateinit var binding: FragmentHomeBinding
    @SuppressLint("SetTextI18n", "MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 오늘 날짜 텍스트뷰 설정
        binding.tvDate.text = SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(Calendar.getInstance().time) + "날씨"

        requestLocation()

        // <새로고침> 버튼 누를 때 위치 정보 & 날씨 정보 다시 가져오기
//        binding.btnRefresh.setOnClickListener {
//            requestLocation()
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //바인딩 ?
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
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
        // 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if (timeH == "00" && baseTime == "2330") {
            cal.add(Calendar.DATE, -1).toString()
            baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        // 날씨 정보 가져오기
        // (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날싸, 발표 시각, 예보지점 좌표)
        val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", baseDate, baseTime, nx, ny)

        // 비동기적으로 실행하기
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // 응답 성공 시
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if (response.isSuccessful) {
                    // 날씨 정보 가져오기
                    val it: List<ITEM> = response.body()!!.response.body.items.item

                    // 현재 시각부터 1시간 뒤의 날씨 6개를 담을 배열
                    val weatherArr = arrayOf(WeatherModel(), WeatherModel(), WeatherModel(), WeatherModel(), WeatherModel(), WeatherModel())

                    // 배열 채우기
                    var index = 0
                    val totalCount = response.body()!!.response.body.totalCount - 1
                    for (i in 0..totalCount) {
                        index %= 6
                        when(it[i].category) {
                            "PTY" -> weatherArr[index].rainType = it[i].fcstValue     // 강수 형태
                            "REH" -> weatherArr[index].humidity = it[i].fcstValue     // 습도
                            "SKY" -> weatherArr[index].sky = it[i].fcstValue          // 하늘 상태
                            "T1H" -> weatherArr[index].temp = it[i].fcstValue         // 기온
                            else -> continue
                        }
                        index++
                    }

                    weatherArr[0].fcstTime = "지금"
                    // 각 날짜 배열 시간 설정
                    for (i in 1..5) weatherArr[i].fcstTime = it[i].fcstTime

                    // 리사이클러 뷰에 데이터 연결
                    binding.weatherRecyclerView.adapter = WeatherAdapter(weatherArr)

                    // 토스트 띄우기
                    //Toast.makeText(applicationContext, it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {

                binding.tvError.text = "api fail : " +  t.message.toString() + "\n 다시 시도해주세요."
                binding.tvError.visibility = View.VISIBLE
                Log.d("api fail", t.message.toString())
            }
        })
    }

    // 내 현재 위치의 위경도를 격자 좌표로 변환하여 해당 위치의 날씨정보 설정하기
    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        //val locationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

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
                            curPoint = Common().dfsXyConv(location.latitude, location.longitude)

                            // 오늘 날짜 텍스트뷰 설정
                            binding.tvDate.text = SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(Calendar.getInstance().time) + " 날씨"
                            // nx, ny지점의 날씨 가져와서 설정하기
                            setWeather(curPoint!!.x, curPoint!!.y)
                        }
                    }
                }
            }

            // 내 위치 실시간으로 감지
            Looper.myLooper()?.let {
                //locationClient.requestLocationUpdates(locationRequest, locationCallback,
                    //it)
            }


        } catch (e : SecurityException) {
            e.printStackTrace()
        }
    }

}