package com.example.flowproject

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ExifInterface
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
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileOutputStream
import java.io.OutputStream




class FragmentThree : Fragment() {

    lateinit var photoURI: Uri
    val REQUEST_CODE = 0
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var curPhotoPath: String
    lateinit var imageView: ImageView
    lateinit var textView: TextView
    lateinit var loadingImage : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun gallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,REQUEST_CODE)
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
        return File.createTempFile("$timeStamp",".jpg",storageDir)
            .apply { curPhotoPath = absolutePath }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //startActivityForResult를 통해서 기본 카메라 앱으로 부터 받아온 사진 결과값
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap
            val file = File(curPhotoPath) // 절대 경로인 사진이 저장된 값
            if (Build.VERSION.SDK_INT < 28) { // 안드로이드9.0(PIE) 버전보다 낮을 경우
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.fromFile(file)) // 끌어온 비트맵을 넣음
            } else { //PIE버전 이상인 경우
                val decode = ImageDecoder.createSource( //변환을 해서 가져옴
                    requireActivity().contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
            }
            imageView.setImageURI(photoURI)
            requireActivity().intent.putExtra("photo",photoURI)
            savePhoto(bitmap)
        }
        //사진을 성공적으로 가져 온 경우
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            var uri = data?.data
            val path = getFullPath(uri!!)
            Log.e("path",path!!)
            var input = requireActivity().contentResolver.openInputStream(uri!!)
            var image = BitmapFactory.decodeStream(input)
            val file : File = bitmapToFile(image,path)
            imageView.setImageURI(uri)
            requireActivity().intent.putExtra("photo",uri)

            var requestBody = file.asRequestBody("image/*".toMediaType())
            var body = MultipartBody.Part.createFormData("image",file.name,requestBody)
            serverResult(body)
        }

    }
    fun serverResult(file:MultipartBody.Part){
        textView.text = "분석중"
        textView.visibility = View.VISIBLE
        Glide.with(this).load(R.drawable.loading).into(loadingImage)        //로딩 GIF이미지를 넣음
        val retrofit = Retrofit.Builder().baseUrl("https://dapi.kakao.com/") //Retrofit2를 사용하여 카카오에 POST메소드 통신
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(RetrofitService::class.java)                  // 헤더와 보내는 형식(Multipart)을 지정함

        service.getOnlineChannel("KakaoAK 16b9d5d1f68577d49a3ddcdae9f7c5ca",    // 개인 키 설정
            file)?.enqueue(object : Callback<Image>{
            override fun onResponse(call: Call<Image>, response: Response<Image>) { // Retrofit2에서 지원하는 방식에 의해서
                var result = response.body()                                        // 미리 만들어 놓은 Image 클래스로 파싱됨
                if(response.isSuccessful) {
                    Log.e("success", result!!.toString())
                    val mainActivity = context as MainActivity
                    if(result.result.faces.isNotEmpty()) {                          //intent를 통해 fragment간 response결과값을 공유함
                        activity!!.intent.putExtra("age",result.result.faces[0].faceAttr.age)
                        activity!!.intent.putExtra("gender",result.result.faces[0].faceAttr.gender.male)
                        mainActivity.changeFragmentFour()
                    }else{
                        textView.text = "사진 인식에 실패했어요"
                        textView.visibility = View.VISIBLE
                        loadingImage.visibility = View.INVISIBLE
                    }
                } else {
                    //Log.e("failed", response.code().toString())
                }
            }
            override fun onFailure(call: Call<Image>, t: Throwable) {
                Log.e("실패2","실패2")
                t.printStackTrace()
            }
        })
    }

    fun getFullPath(uri: Uri) :String? {
        val context = requireContext()
        val contentResolver = context.contentResolver ?: return null

        // Create file path inside app's data dir
        val filePath = (context.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            /*  절대 경로를 getGps()에 넘겨주기   */
            getGps(file.getAbsolutePath())
        }
        return file.getAbsolutePath()
    }
    fun getGps(photoPath: String) {
        var exif: ExifInterface?= null
        try{
            exif = ExifInterface(photoPath)
        }catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun bitmapToFile(bitmap:Bitmap, path : String?) : File {
        val file = File(path)
        var out : OutputStream
        file.createNewFile()
        out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out)
        out.close()
        return file
    }

    fun savePhoto(bitmap: Bitmap) {
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" //사진 폴더에 저장 경로 선언
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Date())
        val fileName = "${timeStamp}.jpeg"
        val folder = File(folderPath)
        if (!folder.isDirectory) { // 현재 해당 경로에 폴더가 존재하지 않는다면
            folder.mkdirs()
        }
        //실제적인 저장처리
        val out = FileOutputStream(folderPath + fileName)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out)
        val file = File(folderPath+fileName)
        var requestBody = file.asRequestBody("image/*".toMediaType())
        var body = MultipartBody.Part.createFormData("image",file.name,requestBody)
        serverResult(body)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_three,container,false)
        imageView = v.findViewById<ImageView>(R.id.faceimage)
        textView = v.findViewById<TextView>(R.id.textView2)
        loadingImage = v.findViewById(R.id.loadingImage)
        val photobutton = v.findViewById<Button>(R.id.photobutton)

        val REQUEST_EXTERNAL_STORAGE = 1
        val PERMISSIONS_STORAGE = arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun verifyStoragePermissions(context: Context) {
            // Check if we have write permission
            val permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                );
            }
        }

        verifyStoragePermissions(v.context)


        textView.visibility = View.INVISIBLE

        Glide.with(this)
            .load("")
            .placeholder(R.drawable.defaultimage)
            .into(imageView);

        photobutton.setOnClickListener{
            showSelectCameraOrImage()
        }

        return v
    }
}