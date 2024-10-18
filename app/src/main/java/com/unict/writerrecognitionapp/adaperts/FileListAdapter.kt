package com.unict.writerrecognitionapp.adaperts

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unict.writerrecognitionapp.R
import com.unict.writerrecognitionapp.activities.ImageViewActivity
import com.unict.writerrecognitionapp.activities.MainActivity
import com.unict.writerrecognitionapp.holders.FileListHolder
import java.io.File

class FileListAdapter(private val dataSet: ArrayList<File>, private val context: MainActivity): RecyclerView.Adapter<FileListHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_list, parent, false)
        return FileListHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: FileListHolder, position: Int) {
        holder.textView.text = dataSet[position].name

        holder.imageView.setOnClickListener {
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra(context.getString(R.string.file_path_key), dataSet[position].path)
            context.startActivity(intent)
        }
    }
}