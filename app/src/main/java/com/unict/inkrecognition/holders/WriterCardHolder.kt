package com.unict.inkrecognition.holders

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.unict.inkrecognition.R

class WriterCardHolder(view: View): RecyclerView.ViewHolder(view) {
    val addFilesBtn: Button = itemView.findViewById(R.id.add_files_btn)
    val inputName: EditText = itemView.findViewById(R.id.writer_name)
    val recyclerView: RecyclerView = itemView.findViewById(R.id.file_list)
}