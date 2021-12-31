package com.example.flowproject

import android.app.Activity
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.core.view.children
import androidx.core.view.marginLeft
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentTwo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentTwo : Fragment() {
    val REQUEST_CODE = 0
    var bitmapList = ArrayList<Bitmap>()
    lateinit var v : View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_two, container, false)

        val fab = v.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            gallery()
        }

        val ll_vertical = v.findViewById<LinearLayout>(R.id.ll_vertical)
        ll_vertical.weightSum=20.0F
        var imageArrayList = ArrayList<ImageView>()
        var linearLayoutList = ArrayList<LinearLayout>()
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            4.0F
        )
        val imageLayoutParams = LinearLayout.LayoutParams(
            350,
            350,
            1F
        )

        val fillLinear = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for (i in 0..4) {
            val linearLayout = LinearLayout(context)
            linearLayout.weightSum = 4.0F
            linearLayout.layoutParams = layoutParams
            linearLayoutList.add(linearLayout)
            for (j in 0..3) {
                var imageView = ImageView(context)
                if (bitmapList.size > j+i*4)  {
                    imageView.setImageBitmap(bitmapList[j+i*4])

                }
                imageView.layoutParams = imageLayoutParams
                imageView.background = requireContext().getDrawable(R.drawable.photo_border)
                imageArrayList.add(imageView)

            }
        }
        for (i in 0..4) {
            for (j in 0..3) {
                linearLayoutList[i].addView(imageArrayList[j+i*4])
                if (bitmapList.size>j+i*4) {
                    imageArrayList[j+i*4].setOnClickListener {
                        if (imageArrayList[j+i*4].layoutParams != fillLinear){
                            for (k in 0..4) {
                                for (w in 0..3) {
                                    if (k == i && w == j) continue
                                    imageArrayList[w+k*4].visibility = View.GONE
                                }
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
                                imageArrayList[j+i*4].layoutParams = imageLayoutParams
                                if (k == i) continue
                                linearLayoutList[k].visibility = View.VISIBLE
                            }
                        }
                    }
                }

            }
            ll_vertical.addView(linearLayoutList[i])
        }

        return v
    }


    private fun gallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //startActivityForResult를 통해서 기본 카메라 앱으로 부터 받아온 사진 결과값
        super.onActivityResult(requestCode, resultCode, data)
        //사진을 성공적으로 가져 온 경우
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            var uri = data?.data
            //val path : String? = data!!.data!!.path
            val path = getFullPath(uri!!)

            var input = requireContext().contentResolver.openInputStream(uri!!)
            var image = BitmapFactory.decodeStream(input)
            val file : File = bitmapToFile(image,path)
            uri = bitmapToUri(image,99)
            bitmapList.add(image)
            Log.e("uri", bitmapList.toString() + "          "+bitmapList.size.toString())

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
        val lat = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        // TAG_GPS_LATITUDE_REF: Indicates whether the latitude is north or south latitude
        val lat_ref = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val lon = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        // TAG_GPS_LONGITUDE_REF: Indicates whether the longitude is east or west longitude.
        val lon_ref = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
    }
    fun bitmapToFile(bitmap:Bitmap, path : String?) : File {
        val file = File(path)
        var out : OutputStream
        Log.e("path","$path")
        file.createNewFile()
        out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG,10,out)
        out.close()
        return file
    }
    fun bitmapToUri(bitmap: Bitmap, i:Int) :Uri {
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" //사진 폴더에 저장 경로 선언
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Date())+"-$i"
        val fileName = "${timeStamp}.jpeg"
        val folder = File(folderPath)
        if (!folder.isDirectory) { // 현재 해당 경로에 폴더가 존재하지 않는다면
            folder.mkdirs()
        }
        //실제적인 저장처리
        val out = FileOutputStream(folderPath + fileName)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out)
        val file = File("/storage/emulated/0/Pictures/${fileName}")
        var uri = Uri.parse(file.absolutePath)

        return uri
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentTwo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentTwo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}