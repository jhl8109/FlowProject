package com.example.flowproject

//import android.app.AlertDialog
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
//import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.view_item_layout.view.*

//import com.google.android.material.button.MaterialButton


class CustomAdapter(private val context: Context, private val dataList: ArrayList<DataVo>, private val urilist: ArrayList<Uri?>) :
    RecyclerView.Adapter<CustomAdapter.ItemViewHolder>() {

    private var viewholderlist: ArrayList<ItemViewHolder> = ArrayList()

    //
    interface MyItemClickListener {
        fun onItemClick(position: Int)
        fun onLongClick(position: Int)
    }
    //
    private lateinit var mItemClickListener: MyItemClickListener

    fun editimage(uri: Uri?, position:Int){
//        val v = view.findViewById<ImageView>(R.id.userImg)
//        urilist[position] = uri
        Glide.with(viewholderlist[position].itemView)
            .load(urilist[position])
            .error(R.drawable.noimage)
            .into(viewholderlist[position].itemView.userImg)
    }


    var mPosition = 0

    fun getPosition(): Int {
        return mPosition
    }

    fun setPosition(position: Int) {
        mPosition = position
    }

    fun addItem(dataVo: DataVo) {
        dataList.add(dataVo)
        //갱신처리 반드시 해야함
        notifyDataSetChanged()
    }

    fun editItem(dataVo: DataVo, position: Int){
        dataList[position] = dataVo
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < dataList.size) {
            dataList.removeAt(position)
            viewholderlist.removeAt(position)
            urilist.removeAt(position)
            //notifyItemRemoved(position)
            //갱신처리 반드시 해야함
            notifyDataSetChanged()
        }
    }
    //
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //
        init {
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                mItemClickListener.onLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }

        private val userPhoto = itemView.findViewById<ImageView>(R.id.userImg)
        private val userName = itemView.findViewById<TextView>(R.id.userNameTxt)
        private val userPay = itemView.findViewById<TextView>(R.id.payTxt)
        private val userAddress: TextView = itemView.findViewById<TextView>(R.id.addressTxt)

        fun bind(dataVo: DataVo, context: Context) {
            //사진 처리
            if (dataVo.photo != "") {
                val resourceId =
                    context.resources.getIdentifier(dataVo.photo, "drawable", context.packageName)

                if (resourceId > 0) {
                    userPhoto.setImageResource(resourceId)
                } else {
                    userPhoto.setImageResource(R.mipmap.ic_launcher_black)
                }
            } else {
                userPhoto.setImageResource(R.mipmap.ic_launcher_black)
            }

            //TextView에 데이터 세팅
            userName.text = dataVo.name
            userPay.text = dataVo.phonenumber.toString()
            userAddress.text = dataVo.address
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_item_layout, parent, false)
        return ItemViewHolder(view)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataList[position], context)
        if(viewholderlist.size == position){
            viewholderlist.add(holder)
        }
        if(urilist.size > position){
            if(urilist[position] != null) {
                Glide.with(holder.itemView)
                    .load(urilist[position])
                    .circleCrop()
                    .into(holder.itemView.userImg)
            }
        }else {
            urilist.add(null)
        }
//        holder.itemView.setOnClickListener { view ->
//            setPosition(position)
//            Toast.makeText(view.context, "이름:" + dataList[position].name + " " + "전화번호:" + dataList[position].phonenumber + " 클릭!", Toast.LENGTH_SHORT).show()
//        }
//
//        holder.itemView.setOnLongClickListener { view ->
//            setPosition(position)
//            Toast.makeText(view.context, "이름:" + dataList[position].name + " " + "전화번호:" + dataList[position].phonenumber + " 롱클릭!", Toast.LENGTH_SHORT).show()
//            return@setOnLongClickListener true
//        }
    }



    override fun getItemCount(): Int {
        return dataList.size
    }
}