package com.example.flowproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

class FragmentThree : Fragment() , SensorEventListener {

    lateinit var mReset : Button
    lateinit var mWalkNum : TextView
    var mSteps : Int = 0
    var mCounterSteps : Int = 0
    lateinit var sensorManager : SensorManager
    lateinit var stepCountSensor : Sensor
    private val multiplePermissionsCode = 100
    private val requiredPermissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_three,container,false)
        val activity = v.context as Activity
        checkPermissions()

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCountSensor == null) {
            Toast.makeText(context,"No step detect sensor", Toast.LENGTH_SHORT)
        }
        mReset = v.findViewById(R.id.btn_reset)
        mWalkNum = v.findViewById(R.id.tv_walk)
        mWalkNum.text = mSteps.toString()
        mReset.setOnClickListener {
            mSteps = 0
            mCounterSteps = 0
            mWalkNum.text = mSteps.toString()
        }
        return v
    }
    override fun onStart() {
        super.onStart()
        if(sensorManager != null) {
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_GAME)
        }
    }

   /*override fun onStop() {
        super.onStop()
        if(sensorManager!= null) {
            sensorManager.unregisterListener(this)
        }
    }*/

    override fun onSensorChanged(p0: SensorEvent?) {
        Log.e("detected" ,"detected")
        if (p0?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (mCounterSteps < 1) {
                mCounterSteps = p0.values[0].toInt()
            }
            mSteps = (p0.values[0] - mCounterSteps).toInt()
            mWalkNum.text = mSteps.toString()
            Log.e("walk","New step detected by step counter sensor. Total step count : " + mSteps)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    private fun checkPermissions() {
        //거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
        var rejectedPermissionList = ArrayList<String>()

        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        for(permission in requiredPermissions){
            if(ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                //만약 권한이 없다면 rejectedPermissionList에 추가
                rejectedPermissionList.add(permission)
            }
        }
        //거절된 퍼미션이 있다면...
        if(rejectedPermissionList.isNotEmpty()){
            //권한 요청!
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(requireActivity(), rejectedPermissionList.toArray(array), multiplePermissionsCode)
        }
    }
}