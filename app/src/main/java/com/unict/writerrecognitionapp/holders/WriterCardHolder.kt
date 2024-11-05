package com.unict.writerrecognitionapp.holders

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.unict.writerrecognitionapp.R

class WriterCardHolder(view: View): RecyclerView.ViewHolder(view) {
    val addFilesGalleryBtn: Button = itemView.findViewById(R.id.add_files_gallery_btn)
    val addFilesCameraBtn: Button = itemView.findViewById(R.id.add_files_camera_btn)
    val inputName: TextInputEditText = itemView.findViewById(R.id.writer_name)
    val recyclerView: RecyclerView = itemView.findViewById(R.id.file_list)
}