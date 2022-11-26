package com.example.today_ootd.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.today_ootd.R
import com.example.today_ootd.model.WeatherModel


class WeatherAdapter() {
    // 강수 형태
    fun getRainImage(rainType : String, sky: String) : Int {
        return when(rainType) {
            "0" -> getWeatherImage(sky)
            "1" -> R.drawable.rainy
            "2" -> R.drawable.hail
            "3" -> R.drawable.snowy
            "4" -> R.drawable.brash
            else -> getWeatherImage(sky)
        }
    }

    fun getWeatherImage(sky : String) : Int {
        // 하늘 상태
        return when(sky) {
            "1" -> R.drawable.sun                       // 맑음
            "3" ->  R.drawable.cloudy                     // 구름 많음
            "4" -> R.drawable.blur                 // 흐림
            else -> R.drawable.ic_launcher_foreground   // 오류
        }
    }

    fun getWeatherMent(temp : String) : String {
        val tempInt = temp.toInt()
        // 날씨에 따라 멘트
        return when(tempInt) {
            in -5..4 -> "패딩 입어야할 날씨!!"
            in 5..9 -> "아우터를 안입으면 추운 날씨에요 :)"
            in 10..14 -> "겉옷 챙겨요~"
            in 15..19 -> "시원한 날씨네요 ^ 0^"
            in 20..24 -> "오늘 날씨 완전 최고!!"
            in 25..30 -> "오늘 덥다.."

            else -> "이상한 날씨네요"   // 오류
        }
    }

}

