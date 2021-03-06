package com.example.flowproject

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import androidx.core.net.toUri

class FragmentFour : Fragment() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_four,container,false)
        val entries = ArrayList<BarEntry>()

        var age = requireActivity().intent.extras?.get("age").toString().toFloat()
        var gender = requireActivity().intent.extras?.get("gender").toString().toFloat() * 100
        var genderint = gender.toInt()


        val chart = v.findViewById<HorizontalBarChart>(R.id.barchart)
        val progressBar1 = v.findViewById<ProgressBar>(R.id.progressBar1)
        val progressBar2 = v.findViewById<ProgressBar>(R.id.progressBar2)
        val backbutton = v.findViewById<Button>(R.id.backbutton)
        progressBar1.progress = 0
        progressBar2.progress = 0

        val progressAnimator1: ObjectAnimator
        progressAnimator1 = ObjectAnimator.ofInt(progressBar1, "progress", genderint)
        progressAnimator1.setDuration(1500)
        progressAnimator1.start()

        val progressAnimator2: ObjectAnimator
        progressAnimator2 = ObjectAnimator.ofInt(progressBar2, "progress", 100-genderint)
        progressAnimator2.setDuration(1500)
        progressAnimator2.start()


        entries.add(BarEntry(0f, age))

        var set1 = BarDataSet(entries, "DataSet 1")

        set1.setColor(ColorTemplate.rgb("#2A7EC1"))

        val data = BarData(set1)

        set1.valueTextSize = 15F
        data.barWidth = 0.5f
        chart.data = data

        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.xAxis.setDrawLabels(false)
        chart.axisLeft.axisMinimum = 0F
        chart.axisLeft.axisMaximum = 100F
        chart.axisLeft.textSize = 15f
        chart.xAxis.setCenterAxisLabels(false)

        //remove right y-axis
        chart.axisRight.isEnabled = false

        //remove legend
        chart.legend.isEnabled = false

        //remove description label
        chart.description.isEnabled = false

        //add animation
        chart.animateY(1500)

        //draw chart
        chart.invalidate()

        val imageView = v.findViewById<ImageView>(R.id.faceimage)
        var photoURI = requireActivity().intent.extras?.get("photo").toString()


        imageView.setImageURI(photoURI.toUri())

        backbutton.setOnClickListener{
            val mainActivity = context as MainActivity
            mainActivity.changeFragmentThree()
        }




        return v
    }

}