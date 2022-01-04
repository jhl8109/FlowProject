package com.example.flowproject

import android.app.Activity
import android.content.Intent
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
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragmentTwo : Fragment() {
    lateinit var curPhotoPath: String //문자열 형태의 사진 경로 값(초기값을 null로 시작하고 싶을 때)
    lateinit var photoURI: Uri
    val REQUEST_IMAGE_CAPTURE = 1 //카메라 사진촬영 요청코드
    val REQUEST_CODE = 0
    var bitmapList = ArrayList<Uri>()
    lateinit var v : View
    lateinit var ll_vertical : LinearLayout
    lateinit var imageArrayList : ArrayList<PhotoView>
    lateinit var linearLayoutList : ArrayList<LinearLayout>
    var db : AppDatabase? = null

    var galleryList = mutableListOf<Gallery>()
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    4.0F)
    val imageLayoutParams = LinearLayout.LayoutParams(
        320,
        320,
        1F
    )
    val fillLinear = LinearLayout.LayoutParams(
        1070,
        1400
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_two, container, false)

        db = AppDatabase.getInstance(requireContext())
        val savedGallery = db!!.photosDao().getAll()

        galleryList = ArrayList()
        if(savedGallery.isNotEmpty()){
            galleryList.addAll(savedGallery)
        }
        bitmapList = ArrayList()
        imageArrayList = ArrayList()
        linearLayoutList = ArrayList()

        for(i in galleryList.indices) {
            bitmapList.add(galleryList[i].photo.toUri())
        }
        val fab = v.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showSelectCameraOrImage()
        }


        ll_vertical = v.findViewById(R.id.ll_vertical)
        ll_vertical.weightSum=20.0F
        imageArrayList = ArrayList()
        linearLayoutList = ArrayList()

        for (i in 0..4) {
            val linearLayout = LinearLayout(context)
            linearLayout.weightSum = 4.0F
            linearLayout.layoutParams = layoutParams
            linearLayoutList.add(linearLayout)

            for (j in 0..3) {
                var imageView = PhotoView(context)
                imageView.minimumScale = 0.99F
                imageView.mediumScale = 0.9999F
                imageView.maximumScale = 1.0001F
                imageLayoutParams.setMargins(5,5,5,5)
                imageView.scaleType = ImageView.ScaleType.FIT_XY
                imageView.layoutParams = imageLayoutParams
                imageView.clipToOutline =true

                if (bitmapList.size > j+i*4)  {
                    imageView.background = requireContext().getDrawable(R.drawable.photo_small_border)
                    Glide.with(this)
                        .load(bitmapList[j+i*4])
                        .error(R.drawable.noimage)
                        .into(imageView)
                }
                if (bitmapList.size == j+i*4) {
                    imageView.setImageResource(R.drawable.ic_add)
                }
                imageArrayList.add(imageView)
            }
        }
        if (bitmapList.size >19) {
            for (x in 0..bitmapList.size/4-5) {
                val bitSize = bitmapList.size
                for (i in 0..3) {
                    var imageView = PhotoView(context)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    imageView.layoutParams = imageLayoutParams
                    imageView.setPadding(0,5,0,5)
                    imageView.background = requireContext().getDrawable(R.drawable.photo_small_border)
                    imageView.clipToOutline =true
                    imageView.minimumScale = 0.99F
                    imageView.mediumScale = 0.9999F
                    imageView.maximumScale = 1.0001F
                    if (20+x*4+i < bitSize)  {
                        Glide.with(this)
                            .load(bitmapList[20+4*x+i])
                            .error(R.drawable.noimage)
                            .into(imageView)
                    }
                    if (bitmapList.size ==bitSize-bitSize%4+i) {
                        imageView.setImageResource(R.drawable.ic_add)
                    }
                    imageArrayList.add(imageView)
                }
                val linearLayout = LinearLayout(context)
                linearLayout.weightSum = 4.0F
                linearLayout.layoutParams = layoutParams
                linearLayoutList.add(linearLayout)
            }
        }

        for(i in 0..(imageArrayList.size-1)/4) {
            for (j in 0..3) {
                linearLayoutList[i].addView(imageArrayList[j+i*4])
                if (bitmapList.size>j+i*4) {
                    imageArrayList[j+i*4].setOnLongClickListener {
                        db!!.photosDao().delete(galleryList[j+i*4])
                        refreshFragment()
                        Toast.makeText(requireContext(), "삭제 성공!", Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                    imageArrayList[j+i*4].setOnClickListener {
                        Log.e("Clicked","${j+i*4} Clicked")
                        if (imageArrayList[j+i*4].layoutParams != fillLinear){
                            for (k in 0..4) {
                                for (w in 0..3) {
                                    if (k == i && w == j) continue
                                    imageArrayList[w+k*4].visibility = View.GONE
                                }
                                imageArrayList[j+i*4].minimumScale = 0.5F
                                imageArrayList[j+i*4].mediumScale = 1.0F
                                imageArrayList[j+i*4].maximumScale = 3.0F
                                imageArrayList[j+i*4].layoutParams = fillLinear

                                if (k == i) continue
                                linearLayoutList[k].visibility = View.GONE
                            }
                        }
                        else {
                            for (k in 0..4) {
                                for (w in 0..3) {
                                    if (k == i && w == j) continue
                                    imageArrayList[w+k*4].visibility = View.VISIBLE
                                }
                                imageArrayList[j+i*4].minimumScale = 0.99F
                                imageArrayList[j+i*4].mediumScale = 0.9999F
                                imageArrayList[j+i*4].maximumScale = 1.0001F
                                imageArrayList[j+i*4].layoutParams = imageLayoutParams

                                if (k == i) continue
                                linearLayoutList[k].visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
            if (linearLayoutList[i].parent != null) {
                val parent = linearLayoutList[i].parent as ViewGroup
                parent.removeView(linearLayoutList[i])
            }
            ll_vertical.addView(linearLayoutList[i])
        }
        return v
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
    fun takeCapture() { //카메라 촬영
        // 기본 카메라 앱 실행
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


    private fun gallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,REQUEST_CODE)
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
            //bitmapList.add(photoURI)
            db?.photosDao()?.insertAll(Gallery(null,photoURI.toString()))
            refreshFragment()

        }
        //사진을 성공적으로 가져 온 경우
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            var uri = data?.data
            val path = getFullPath(uri!!)
            var input = requireContext().contentResolver.openInputStream(uri!!)
            var image = BitmapFactory.decodeStream(input)
            val orientedBitmap = ExifUtil.rotateBitmap(path,image)
            //bitmapList.add(uri)
            db?.photosDao()?.insertAll(Gallery(null,uri.toString()))
            refreshFragment()
        }

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
    fun refreshFragment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requireFragmentManager().beginTransaction().detach(this).commitNow();
            requireFragmentManager().beginTransaction().attach(this).commitNow();
        } else {
            requireFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }
}