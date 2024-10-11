package com.unict.inkrecognition.adaperts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unict.inkrecognition.R
import com.unict.inkrecognition.activities.MainActivity
import com.unict.inkrecognition.holders.FileListHolder
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
            //TODO
        }
    }
}