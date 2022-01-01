package com.example.flowproject

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.view_item_layout.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FragmentThree : Fragment() {

    lateinit var photoURI: Uri
    val REQUEST_CODE = 0
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var curPhotoPath: String
    lateinit var imageView: ImageView

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
        Log.e("test", "reach here 2")
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
        return File.createTempFile("JPEG_${timeStamp}_",".jpg",storageDir)
            .apply { curPhotoPath = absolutePath }
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

            imageView.setImageURI(photoURI)



        }

        //사진을 성공적으로 가져 온 경우
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            Log.e("test","reachhere3")
            var uri = data?.data

            imageView.setImageURI(uri)

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_three,container,false)

        imageView = v.findViewById<ImageView>(R.id.faceiamge)
        val photobutton = v.findViewById<Button>(R.id.photobutton)
        val sendbutton = v.findViewById<Button>(R.id.sendbutton)


        photobutton.setOnClickListener{
            showSelectCameraOrImage()
        }

        sendbutton.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.result, null)
            val alertDialog = AlertDialog.Builder(v.context)
                .setView(dialogView)
                .create()

            alertDialog.show()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.borderline)
        }

        return v
    }
    override fun onStart() {
        super.onStart()

    }




}