package com.example.today_ootd.home

import android.annotation.SuppressLint
import android.graphics.Point
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.today_ootd.MainActivity
import com.example.today_ootd.adapter.WeatherAdapter
import com.example.today_ootd.component.Common
import com.example.today_ootd.databinding.FragmentHomeBinding
import com.example.today_ootd.model.ITEM
import com.example.today_ootd.model.WEATHER
import com.example.today_ootd.network.WeatherObject
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var baseDate = "20210510"  // 발표 일자
    private var baseTime = "1400"      // 발표 시각
    private var curPoint : Point? = null    // 현재 위치의 격자 좌표를 저장할 포인트

    private var binding: FragmentHomeBinding? = null
    private lateinit var adapter: WeatherAdapter

    @SuppressLint("SetTextI18n", "MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //바인딩
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        adapter = WeatherAdapter() //초기화

        //binding!!.tvDate.text = SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(Calendar.getInstance().time) + "날씨"

        requestLocation()

        // <새로고침> 버튼 누를 때 위치 정보 & 날씨 정보 다시 가져오기
        binding!!.btnRefresh.setOnClickListener {
            requestLocation()
        }

        return binding!!.root
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
        //val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", "20221125", "0500", nx, ny)
        val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", baseDate, baseTime, nx, ny)
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
        binding?.imgWeather?.setImageResource(adapter.getRainImage(rainType, sky))
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

}