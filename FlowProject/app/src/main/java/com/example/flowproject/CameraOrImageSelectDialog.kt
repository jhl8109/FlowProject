package com.example.flowproject

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.KeyListener
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_camera_image_select.*

class CameraOrImageSelectDialog(private val listener: OnClickSelectListener) : DialogFragment() {
    interface OnClickSelectListener {
        fun onClickCamera() fun onClickImage()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.dialog_camera_image_select, container, false)

        return view
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = 500
            dialog!!.window!!.setLayout(width, height)
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialog!!.setCancelable(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraIv.setOnClickListener {
            listener.onClickCamera()
            dismiss()
        }
        galleryIv.setOnClickListener {
            listener.onClickImage()
            dismiss()
        }
    }
}
