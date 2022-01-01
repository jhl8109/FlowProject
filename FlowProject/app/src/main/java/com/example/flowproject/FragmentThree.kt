package com.example.flowproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FragmentThree : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_three,container,false)

        val retrofit = Retrofit.Builder().baseUrl("https://dapi.kakao.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(RetrofitService::class.java)

        service.getOnlineChannel("KakaoAK 16b9d5d1f68577d49a3ddcdae9f7c5ca",
            "https://images.khan.co.kr/article/2021/07/16/l_2021071602000853400181201.jpg")?.enqueue(object : Callback<Image>{
            override fun onResponse(call: Call<Image>, response: Response<Image>) {
                if(response.isSuccessful) {
                    var result = response.body()
                    result!!.result.faces[0].faceAttr.age
                    Log.e("success", result!!.result.faces[0].faceAttr.age.toString())
                } else {
                    Log.e("failed", "실패")
                }
            }
            override fun onFailure(call: Call<Image>, t: Throwable) {
                Log.e("실패2","실패2")
                t.printStackTrace()

            }
        })

        return v
    }

}