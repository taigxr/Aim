package com.example.aim

import  android.view.View
import android.media.Image
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast


class RecyclerAdapter(
    private var username: List<String>,
    private var captions: List<String>,
    private var image: List<Int>,
    private var pfp: List<Int>
) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemUsername: TextView = itemView.findViewById(R.id.f_username)
        val itemImage: ImageView = itemView.findViewById(R.id.f_image)
        val itemPfp: ImageView = itemView.findViewById(R.id.f_pfp)
        val itemCaption: TextView = itemView.findViewById(R.id.f_caption)

        init {
            itemView.setOnClickListener { v: View ->
                val position: Int = adapterPosition
                Toast.makeText(
                    itemView.context,
                    "You clicked on item # ${position + 1}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return username.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemUsername.text = username[position]
        holder.itemCaption.text = captions[position]
        holder.itemPfp.setImageResource(pfp[position])
        holder.itemImage.setImageResource(image[position])
    }
}
