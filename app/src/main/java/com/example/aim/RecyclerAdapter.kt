package com.example.aim

import  android.view.View
import android.media.Image
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(
    var username: List<String>,
    var captions: List<String>,
    var date: List<Int>,
    var pic: List<Int>
) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var user: TextView = itemView.findViewById(R.id.caption)
        var image: ImageView = itemView.findViewById(R.id.userpicture)
        var pfp: ImageView = itemView.findViewById(R.id.pfp)
        var caption: TextView = itemView.findViewById(R.id.caption)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return username.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.user.text = username[position]
        holder.caption.text = captions[position]
        holder.image.setImageResource(pic[position])
    }
}