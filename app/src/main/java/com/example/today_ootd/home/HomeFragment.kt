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
import com.example.today_ootd.databinding.FragmentHomeDetailBinding
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
    private var binding1: FragmentHomeDetailBinding? = null
    private val articleAdapter = ArticleAdapter()
    private val articleList = mutableListOf<ArticleModel>()

    private var baseDate = "20210510"  // ?????? ??????
    private var baseTime = "1400"      // ?????? ??????
    private var curPoint : Point? = null    // ?????? ????????? ?????? ????????? ????????? ?????????
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
        //val fragmentHomeDetailBinding = FragmentHomeDetailBinding.bind(view)
        binding = fragmentHomeBinding
        //binding1 = fragmentHomeDetailBinding
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
                .replace(R.id.fragmentContainer, homeDetailFragment.apply {
                    arguments = Bundle().apply {
                        //putString("key","value")
                        putString("nickname",data.nickname)
                        putString("weather",data.whether)
                        putInt("height",data.height)
                        putString("url",data.imageUrl)
                        putString("style",data.style)
                        putInt("like",data.likeCount)
                        putString("outer",data.outer)
                        putString("top",data.top)
                        putString("bottom",data.bottom)
                        putString("shoes",data.shoes)
                        putString("bag",data.bag)
                        putString("acc",data.acc)

                    }
                })
                .addToBackStack(null)
                .commit()
        }
    })

        //-------------------?????? Setting
        weatherAdapter = WeatherAdapter() //?????????

        requestLocation()

        // <????????????> ?????? ?????? ??? ?????? ?????? & ?????? ?????? ?????? ????????????
        binding!!.btnRefresh.setOnClickListener {
            requestLocation()
        }

    }


    // ?????? ???????????? ????????????
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setWeather(nx : Int, ny : Int) {
        // ?????? ?????? : base_date(?????? ??????), base_time(?????? ??????)
        // ?????? ??????, ?????? ?????? ????????????
        val cal = Calendar.getInstance()
        baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // ?????? ??????
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // ?????? ??????
        val timeM = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // ?????? ???
        // API ???????????? ???????????? ??????
        baseTime = Common().getBaseTime(timeH, timeM)
        System.out.println("baseTime : " +baseTime)

        // ?????? ????????? 00????????? 45??? ???????????? baseTime??? 2330?????? ?????? ?????? ????????????
        if (timeH == "00" && baseTime == "2330") {
            cal.add(Calendar.DATE, -1).toString() //?????? ???
            baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        cal.add(Calendar.DATE, -1).toString() //?????? ???
        baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        System.out.println("2. baseDate: " + baseDate + " timeH: " + timeH + " timeM:" + timeM + " baseTime: " + baseTime)

        // ?????? ?????? ????????????
        // (??? ????????? ?????? ??? = 60, ????????? ?????? = 1, ?????? ?????? ??????-"JSON", ?????? ??????, ?????? ??????, ???????????? ??????)
        //val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", "20221128", "0500", nx, ny)
        val call = WeatherObject.getRetrofitService().getWeather(60, 1, "JSON", baseDate, "0500", nx, ny)
        System.out.println("2-1. baseDate: " + baseDate + " baseTime: " + baseTime + " nx:" + nx + " ny: " + ny)
        System.out.println("3. call" + call)

        // ?????????????????? ????????????
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // ?????? ?????? ???
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                System.out.println("4. onResponse ?????? ??? ")
                if (response.isSuccessful) {
                    // ?????? ?????? ????????????
                    val it: List<ITEM> = response.body()!!.response.body.items.item

                    var rainRatio = ""      // ?????? ??????
                    var rainType = ""       // ?????? ??????
                    var humidity = ""       // ??????
                    var sky = ""            // ?????? ??????
                    var temp = ""           // ??????

                    for (i in 0..59) {
                        when(it[i].category) {
                            "POP" -> rainRatio = it[i].fcstValue    // ?????? ??????
                            "PTY" -> rainType = it[i].fcstValue     // ?????? ??????
                            "REH" -> humidity = it[i].fcstValue     // ??????
                            "SKY" -> sky = it[i].fcstValue          // ?????? ??????
                            "TMP" -> temp = it[i].fcstValue         // ??????
                            else -> continue
                        }
                    }
                    // ?????? ?????? ??????????????? ????????? ??????
                    writeWeather(rainRatio, rainType, humidity, sky, temp)

                }
            }

            // ?????? ?????? ???
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                binding?.tvError?.text = "api fail : " +  t.message.toString() + "\n ?????? ??????????????????."
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
        binding?.tvTemp?.text = temp + "?? "
        binding?.tvHumidity?.text = humidity + "% "

    }


    // ??? ?????? ????????? ???????????? ?????? ????????? ???????????? ?????? ????????? ???????????? ????????????
    @SuppressLint("MissingPermission")
    private fun requestLocation() {

        //getFusedLocationProviderClient??? activity????????? ??????????????? mainActivity?????? ???????????? ????????????
        val locationClient = (activity as MainActivity).sendLocation()
        System.out.println("1. ????????? ${locationClient}")


        try {
            // ?????? ?????? ?????? ??????
            val locationRequest = LocationRequest.create()
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 60 * 1000    // ?????? ??????(1???)
            }
            val locationCallback = object : LocationCallback() {
                // ?????? ??????
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onLocationResult(p0: LocationResult) {
                    p0.let {
                        for (location in it.locations) {
                            // ?????? ????????? ???????????? ?????? ????????? ??????
                            System.out.println("1-1. location.latitude??? ${location.latitude} location.longitude??? ${location.longitude}")

                            curPoint = Common().dfsXyConv(location.latitude, location.longitude)

                            System.out.println("1-2. curPoint??? ${curPoint}")

                            // ?????? ?????? ???????????? ??????
                            // nx, ny????????? ?????? ???????????? ????????????
                            //setWeather(curPoint!!.x, curPoint!!.y)
                            //????????? ??????
                            setWeather(61, 127)
                        }
                    }
                }
            }

            // ??? ?????? ??????????????? ??????
            Looper.myLooper()?.let {
                locationClient.requestLocationUpdates(locationRequest, locationCallback, it)
            }


        } catch (e : SecurityException) {
            e.printStackTrace()
        }
    }


    //    private val mChildListener = object : ChildEventListener {
//        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//            // DB??? article??? ?????????????????? ???????????? ?????????
//            // article ????????? ????????? ????????? ????????????
//            val articleModel = snapshot.getValue(ArticleModel::class.java)
//            Log.d(TAG,"addChildEventListener is Called!! now size : ${articleList.size}")
//            articleModel ?: return // null??? ??????
//            articleList.add(articleModel)
//            articleAdapter.submitList(articleList)
//            articleAdapter.notifyDataSetChanged() // ??????
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
