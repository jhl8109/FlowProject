package com.example.flowproject

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
//import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.dialog_sample.view.*
import kotlinx.android.synthetic.main.fragment_one.view.*
import kotlinx.android.synthetic.main.view_item_layout.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
    lateinit var v : View
    lateinit var recyclerView: RecyclerView
    private var userList: ArrayList<DataVo> = ArrayList()
    private var urilist: ArrayList<Uri?> = ArrayList()
    val REQUEST_CODE = 0
    lateinit var photoURI: Uri
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var curPhotoPath: String
    lateinit var mAdapter: CustomAdapter
    lateinit var tempposition: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun showSelectCameraOrImage() {
        CameraOrImageSelectDialog(object: CameraOrImageSelectDialog.OnClickSelectListener {
            override fun onClickCamera() {
                takeCapture()
            }
            override fun onClickImage() {
                gallery()
            }
        }).show(requireFragmentManager(), "CameraOrImageSelectDialog")
    }

    private fun takeCapture(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                val photofile: File? =try {
                    createImageFile()
                } catch(ex: IOException) {
                    null
                }
                photofile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.flowproject.fileprovider", //보안 서명
                        it
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    fun createImageFile(): File { // 이미지파일 생성
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.e("storageDir","$storageDir")
        return File.createTempFile("JPEG_${timeStamp}_",".jpg",storageDir)
            .apply { curPhotoPath = absolutePath }
    }

    private fun gallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //startActivityForResult를 통해서 기본 카메라 앱으로 부터 받아온 사진 결과값
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap
            val file = File(curPhotoPath) // 절대 경로인 사진이 저장된 값
            if (Build.VERSION.SDK_INT < 28) { // 안드로이드9.0(PIE) 버전보다 낮을 경우
                Log.d("Check",file.toString())
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.fromFile(file)) // 끌어온 비트맵을 넣음
            } else { //PIE버전 이상인 경우
                val decode = ImageDecoder.createSource( //변환을 해서 가져옴
                    requireActivity().contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
            }

            urilist[tempposition.toInt()] = photoURI
            mAdapter.editimage(photoURI, tempposition.toInt())
        }

        //사진을 성공적으로 가져 온 경우
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            var uri = data?.data

            urilist[tempposition.toInt()] = uri
            mAdapter.editimage(uri, tempposition.toInt())
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
        v =  inflater.inflate(R.layout.fragment_one, container, false)
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
                    urilist.add(null)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        mAdapter = CustomAdapter(v.context, userList, urilist)
        mAdapter.setMyItemClickListener(object : CustomAdapter.MyItemClickListener {
            override fun onItemClick(position: Int) {
                mAdapter.setPosition(position)
                Toast.makeText(v.context, "이름:" + userList[position].name + "\n" + "전화번호:" + userList[position].phonenumber, Toast.LENGTH_SHORT).show()
            }

            override fun onLongClick(position: Int) {
                val dialogView = layoutInflater.inflate(R.layout.longclick, null)
                val alertDialog = AlertDialog.Builder(v.context)
                    .setView(dialogView)
                    .create()

//                val userName = dialogView.findViewById<EditText>(R.id.username).text
//                val phoneNumber = dialogView.findViewById<EditText>(R.id.phoneNumber).text
//                val location = dialogView.findViewById<EditText>(R.id.location).text
                val modify = dialogView.findViewById<Button>(R.id.modify)
                val phone = dialogView.findViewById<Button>(R.id.phone)
                val delete = dialogView.findViewById<Button>(R.id.delete)
                val message = dialogView.findViewById<Button>(R.id.message)
                val photo = dialogView.findViewById<Button>(R.id.photo)
                tempposition = position.toString()

                modify.setOnClickListener {
                    alertDialog.dismiss()
                    var dialogView = layoutInflater.inflate(R.layout.dialog_sample, null)
                    dialogView.username.hint = userList[position].name
                    dialogView.phoneNumber.hint = userList[position].phonenumber
                    dialogView.location.hint = userList[position].address

                    val alertDialog = AlertDialog.Builder(v.context)
                        .setView(dialogView)
                        .create()

                    val userName = dialogView.findViewById<EditText>(R.id.username).text
                    val phoneNumber = dialogView.findViewById<EditText>(R.id.phoneNumber).text
                    val location = dialogView.findViewById<EditText>(R.id.location).text
                    val button = dialogView.findViewById<Button>(R.id.button)


//                    if(userName.equals("")){
//                        userName = userList[position].name
//                    }
//                    if(phoneNumber.equals("")){
//                        phoneNumber = userList[position].phonenumber
//                    }
//                    if(location.equals("")){
//                        location = userList[position].address
//                    }

                    button.setOnClickListener {
                        var putusername = userName.toString()
                        var putphoneNumber = phoneNumber.toString()
                        var putlocation = location.toString()

                        if(putusername.equals("")){
                            putusername = userList[position].name
                        }
                        if(putphoneNumber.equals("")){
                            putphoneNumber = userList[position].phonenumber
                        }
                        if(putlocation.equals("")){
                            putlocation = userList[position].address
                        }
                        mAdapter.editItem(DataVo(putusername, putlocation, putphoneNumber, "user_img_01"), position)
                        alertDialog.dismiss()
                    }

                    alertDialog.show()
                    alertDialog.window?.setBackgroundDrawableResource(R.drawable.borderline)
                }
                phone.setOnClickListener {
                    alertDialog.dismiss()
                    var intent = Intent(Intent.ACTION_DIAL)
                    val phonenumber = userList[position].phonenumber
                    intent.data = Uri.parse("tel:"+phonenumber)
                    startActivity(intent)
                }
                message.setOnClickListener {
                    alertDialog.dismiss()
                    var intent = Intent(Intent.ACTION_SENDTO)
                    val phonenumber = userList[position].phonenumber
                    intent.data = Uri.parse("smsto:"+phonenumber)
                    startActivity(intent)
                }
                photo.setOnClickListener {
                    showSelectCameraOrImage()
                    alertDialog.dismiss()
                }
                delete.setOnClickListener {
                    alertDialog.dismiss()
                    mAdapter.removeItem(position)
                }

                alertDialog.show()
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.borderline)
            }

        })

        recycler_view.adapter = mAdapter

        val layout = LinearLayoutManager(requireContext())
        recycler_view.layoutManager = layout
        recycler_view.setHasFixedSize(true)
        val add_btn = v.findViewById<FloatingActionButton>(R.id.add_btn)
//        val modify_btn = v.findViewById<Button>(R.id.modify_btn)
//        val phone_btn = v.findViewById<Button>(R.id.phone_btn)
//        val del_btn = v.findViewById<Button>(R.id.del_btn)

        add_btn.setOnClickListener {
//            mAdapter.addItem(DataVo("아무개", "test14", "광주시", "12345678", "user_img_03"))
            val dialogView = layoutInflater.inflate(R.layout.dialog_sample, null)
            val alertDialog = AlertDialog.Builder(v.context)
                .setView(dialogView)
                .create()

            val userName = dialogView.findViewById<EditText>(R.id.username).text
            val phoneNumber = dialogView.findViewById<EditText>(R.id.phoneNumber).text
            val location = dialogView.findViewById<EditText>(R.id.location).text
            val button = dialogView.findViewById<Button>(R.id.button)

            button.setOnClickListener {
                val putuserName = userName.toString()
                val putphoneNumber = phoneNumber.toString()
                val putlocation = location.toString()

                if(putuserName.equals("")){
                    Toast.makeText(v.context, "Type name!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(putphoneNumber.equals("")){
                    Toast.makeText(v.context, "Type phone number!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(putlocation.equals("")){
                    Toast.makeText(v.context, "Type address!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                alertDialog.dismiss()
                mAdapter.addItem(DataVo(userName.toString(), location.toString(), phoneNumber.toString(), "user_img_01"))
                urilist.add(null)
            }

            alertDialog.show()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.borderline)
            // notifyDataSetChanged를 호출하여 adapter의 값이 변경되었다는 것을 알려준다.
            // 어댑터 안에서 처리했음으로 주석처리하였다.
            //mAdapter.notifyDataSetChanged()
        }
//
//        modify_btn.setOnClickListener {
//            val dialogView = layoutInflater.inflate(R.layout.dialog_sample, null)
//            val alertDialog = AlertDialog.Builder(v.context)
//                .setView(dialogView)
//                .create()
//
//            val userName = dialogView.findViewById<EditText>(R.id.username).text
//            val phoneNumber = dialogView.findViewById<EditText>(R.id.phoneNumber).text
//            val location = dialogView.findViewById<EditText>(R.id.location).text
//            val button = dialogView.findViewById<MaterialButton>(R.id.button)
//
//            button.setOnClickListener {
//                alertDialog.dismiss()
//                mAdapter.addItem(DataVo(userName.toString(), location.toString(), phoneNumber.toString(), "user_img_01"))
//            }
//
//            alertDialog.show()
//            // notifyDataSetChanged를 호출하여 adapter의 값이 변경되었다는 것을 알려준다.
//            // 어댑터 안에서 처리했음으로 주석처리하였다.
//            //mAdapter.notifyDataSetChanged()
//        }

//        del_btn.setOnClickListener {
//            mAdapter.removeItem(mAdapter.getPosition())
//            // notifyDataSetChanged를 호출하여 adapter의 값이 변경되었다는 것을 알려준다.
//            //mAdapter.notifyDataSetChanged()
//        }

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