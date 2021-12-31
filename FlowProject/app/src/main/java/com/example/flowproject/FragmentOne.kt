package com.example.flowproject

import android.app.AlertDialog
import android.content.res.AssetManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentOne.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentOne : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var a = 0

    private var userList: ArrayList<DataVo> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

//    var userList = arrayListOf<DataVo>(
//        DataVo("IU", "test1", "전주시", "010-1111-1111","user_img_01"),
//        DataVo("홍길동", "test2", "서울시","010-1234-5678", "user_img_02"),
//        DataVo("김영수", "test3", "광주시", "010-0000-0000", "user_img_03")
//    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_one, container, false)
        val recycler_view = v.findViewById<RecyclerView>(R.id.recycler_view)

        fun loadJSONFileFromAsset(): String {
            return try {
                val assetManager: AssetManager = resources.assets
                val inputStream = assetManager.open("users_list.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                //val charset: Charset = Charsets.UTF_8
                inputStream.read(buffer)
                inputStream.close()

                String(buffer, Charsets.UTF_8)
            } catch (ex: IOException) {
                ex.printStackTrace()
                return ""
            }
        }
        if (a == 0) {
            a++
            try {
                val assetManager: AssetManager = resources.assets
                val inputStream = assetManager.open("sample_code.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                var strJson = String(buffer, Charsets.UTF_8)
                val jsonObject = JSONObject(strJson)

                //파일 읽어오는 IO처리 부분 메서드 분기 처리방법
                val jsonObject2 = JSONObject(loadJSONFileFromAsset())
                val userArray = jsonObject.getJSONArray("usersInfo")
                for (i in 0 until userArray.length()) {
                    val baseInfo = userArray.getJSONObject(i)
                    val tempData = DataVo(
                        baseInfo.getString("name"),
                        baseInfo.getString("address"),
                        baseInfo.getString("phonenumber"),
                        baseInfo.getString("photo")
                    )
                    userList.add(tempData)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val mAdapter = CustomAdapter(v.context, userList)
        recycler_view.adapter = mAdapter

        val layout = LinearLayoutManager(requireContext())
        recycler_view.layoutManager = layout
        recycler_view.setHasFixedSize(true)

        val add_btn = v.findViewById<Button>(R.id.add_btn)
        val del_btn = v.findViewById<Button>(R.id.del_btn)

        add_btn.setOnClickListener {
//            mAdapter.addItem(DataVo("아무개", "test14", "광주시", "12345678", "user_img_03"))
            val dialogView = layoutInflater.inflate(R.layout.dialog_sample, null)
            val alertDialog = AlertDialog.Builder(v.context)
                .setView(dialogView)
                .create()

            val userName = dialogView.findViewById<EditText>(R.id.username).text
            val phoneNumber = dialogView.findViewById<EditText>(R.id.phoneNumber).text
            val location = dialogView.findViewById<EditText>(R.id.location).text
            val button = dialogView.findViewById<MaterialButton>(R.id.button)

            button.setOnClickListener {
                alertDialog.dismiss()
                mAdapter.addItem(DataVo(userName.toString(), location.toString(), phoneNumber.toString(), "user_img_01"))
            }

            alertDialog.show()
            // notifyDataSetChanged를 호출하여 adapter의 값이 변경되었다는 것을 알려준다.
            // 어댑터 안에서 처리했음으로 주석처리하였다.
            //mAdapter.notifyDataSetChanged()
        }

        del_btn.setOnClickListener {
            mAdapter.removeItem(mAdapter.getPosition())
            // notifyDataSetChanged를 호출하여 adapter의 값이 변경되었다는 것을 알려준다.
            //mAdapter.notifyDataSetChanged()
        }

        return v
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentOne.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentOne().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}