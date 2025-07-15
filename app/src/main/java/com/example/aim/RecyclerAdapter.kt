package com.example.aim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter(
    private var username: List<String>,
    private var captions: List<String>,
    private var imageUrls: List<String>,
    private var pfpUrls: List<String>,
    private var timestamps: List<String> // added timestamps as formatted String
) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemUsername: TextView = itemView.findViewById(R.id.f_username)
        val itemCaption: TextView = itemView.findViewById(R.id.f_caption)
        val itemTimestamp: TextView = itemView.findViewById(R.id.f_timestamp)
        val itemImage: ImageView = itemView.findViewById(R.id.f_image)
        val itemPfp: ImageView = itemView.findViewById(R.id.f_pfp)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                Toast.makeText(
                    itemView.context,
                    "You clicked on item #${position + 1}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = username.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemUsername.text = username[position]
        holder.itemCaption.text = captions[position]
        holder.itemTimestamp.text = timestamps[position]

        // Use Glide to load images from URL
        Glide.with(holder.itemView.context)
            .load(imageUrls[position])
            .into(holder.itemImage)

        Glide.with(holder.itemView.context)
            .load(pfpUrls[position])
            .into(holder.itemPfp)
    }
}
