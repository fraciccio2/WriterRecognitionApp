package com.unict.inkrecognition.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unict.inkrecognition.R

class FileListHolder(view: View): RecyclerView.ViewHolder(view) {
    val textView: TextView = itemView.findViewById(R.id.text_view_writer)
    val imageView: ImageView = itemView.findViewById(R.id.writer_show_image)
}