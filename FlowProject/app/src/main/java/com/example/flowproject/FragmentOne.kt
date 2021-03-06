package com.example.flowproject

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.dialog_sample.view.*
import kotlinx.android.synthetic.main.fragment_one.view.*
import kotlinx.android.synthetic.main.view_item_layout.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
    var db : AppDatabase? = null
    lateinit var savedContacts: List<Contact>


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
                        "com.example.flowproject.fileprovider", //?????? ??????
                        it
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    fun createImageFile(): File { // ??????????????? ??????
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //startActivityForResult??? ????????? ?????? ????????? ????????? ?????? ????????? ?????? ?????????
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap
            val file = File(curPhotoPath) // ?????? ????????? ????????? ????????? ???
            if (Build.VERSION.SDK_INT < 28) { // ???????????????9.0(PIE) ???????????? ?????? ??????
                Log.d("Check",file.toString())
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.fromFile(file)) // ????????? ???????????? ??????
            } else { //PIE?????? ????????? ??????
                val decode = ImageDecoder.createSource( //????????? ?????? ?????????
                    requireActivity().contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
            }

            urilist[tempposition.toInt()] = photoURI
            savedContacts[tempposition.toInt()].uri = photoURI.toString()
            db!!.contactDao().updateUsers(savedContacts[tempposition.toInt()])
            mAdapter.editimage(photoURI, tempposition.toInt())
        }

        //????????? ??????????????? ?????? ??? ??????
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            var uri = data?.data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().grantUriPermission(requireContext().applicationContext.packageName,uri!!,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Log.e("uri", uri.toString())
            Log.e("savedContacts size", savedContacts.size.toString())
            Log.e("position", tempposition)

            urilist[tempposition.toInt()] = uri
            savedContacts[tempposition.toInt()].uri = uri.toString()
            db!!.contactDao().updateUsers(savedContacts[tempposition.toInt()])
            mAdapter.editimage(uri, tempposition.toInt())
        }

    }

//    var userList = arrayListOf<DataVo>(
//        DataVo("IU", "test1", "?????????", "010-1111-1111","user_img_01"),
//        DataVo("?????????", "test2", "?????????","010-1234-5678", "user_img_02"),
//        DataVo("?????????", "test3", "?????????", "010-0000-0000", "user_img_03")
//    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_one, container, false)
        val recycler_view = v.findViewById<RecyclerView>(R.id.recycler_view)
        userList = ArrayList()
        urilist = ArrayList()



//        val REQUEST_EXTERNAL_STORAGE = 1
//        val PERMISSIONS_STORAGE = arrayOf<String>(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        )
//
//        fun verifyStoragePermissions(context: Context) {
//            // Check if we have write permission
//            val permission1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            val permission2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//            if (permission1 != PackageManager.PERMISSION_GRANTED) {
//                // We don't have permission so prompt the user
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//                );
//            }
//            if (permission2 != PackageManager.PERMISSION_GRANTED) {
//                // We don't have permission so prompt the user
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//                );
//            }
//        }
//
//        verifyStoragePermissions(v.context)

        db = AppDatabase.getInstance(requireContext())
        savedContacts = db!!.contactDao().getAll()
        Log.e("test 1", savedContacts.size.toString())
        if(savedContacts.size > 0){
            for(i: Int in 0..savedContacts.size-1){
                userList.add(DataVo(savedContacts[i].name, savedContacts[i].address,savedContacts[i].phonenumber,savedContacts[i].photo))
                urilist.add(savedContacts[i].uri.toUri())
            }
        }

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
        if (savedContacts.size == 0) {
            try {
                val assetManager: AssetManager = resources.assets
                val inputStream = assetManager.open("sample_code.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                var strJson = String(buffer, Charsets.UTF_8)
                val jsonObject = JSONObject(strJson)

                //?????? ???????????? IO?????? ?????? ????????? ?????? ????????????
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
                    db?.contactDao()?.insertAll(Contact(null, tempData.name, tempData.address, tempData.phonenumber, tempData.photo, "null"))
                    savedContacts = db!!.contactDao().getAll()
                    urilist.add(null)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        mAdapter = CustomAdapter(v.context, userList, urilist)
        Log.e("userList check", userList.size.toString())
        for(i:Int in 0..urilist.size-1){
            Log.e("urilist check", urilist[i].toString())
        }
        mAdapter.setMyItemClickListener(object : CustomAdapter.MyItemClickListener {
            override fun onItemClick(position: Int) {
                mAdapter.setPosition(position)
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

                        savedContacts[position].name = putusername
                        savedContacts[position].address = putlocation
                        savedContacts[position].phonenumber = putphoneNumber

                        db!!.contactDao().updateUsers(savedContacts[position])
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
                    db!!.contactDao().delete(savedContacts[position])
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
//            mAdapter.addItem(DataVo("?????????", "test14", "?????????", "12345678", "user_img_03"))
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
                db?.contactDao()?.insertAll(Contact(null, userName.toString(), location.toString(), phoneNumber.toString(), "user_img_01", "null"))
                savedContacts = db!!.contactDao().getAll()

                urilist.add(null)
            }

            alertDialog.show()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.borderline)
            // notifyDataSetChanged??? ???????????? adapter??? ?????? ?????????????????? ?????? ????????????.
            // ????????? ????????? ?????????????????? ?????????????????????.
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
//            // notifyDataSetChanged??? ???????????? adapter??? ?????? ?????????????????? ?????? ????????????.
//            // ????????? ????????? ?????????????????? ?????????????????????.
//            //mAdapter.notifyDataSetChanged()
//        }

//        del_btn.setOnClickListener {
//            mAdapter.removeItem(mAdapter.getPosition())
//            // notifyDataSetChanged??? ???????????? adapter??? ?????? ?????????????????? ?????? ????????????.
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