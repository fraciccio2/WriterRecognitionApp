package com.unict.writerrecognitionapp.adaperts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unict.writerrecognitionapp.R
import com.unict.writerrecognitionapp.activities.MainActivity
import com.unict.writerrecognitionapp.holders.WriterCardHolder
import com.unict.writerrecognitionapp.models.Writer
import java.io.File

class WriterCardAdapter(private val dataSet: ArrayList<Writer>, private val context: MainActivity) :
    RecyclerView.Adapter<WriterCardHolder>() {
    private lateinit var item: Writer
    private var adapters: ArrayList<FileListAdapter> = arrayListOf()
    private lateinit var adapter: FileListAdapter

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WriterCardHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.writer_card, parent, false)
        return WriterCardHolder(view)
    }

    override fun onBindViewHolder(holder: WriterCardHolder, position: Int) {
        item = dataSet[position]

        holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        adapters.add(FileListAdapter(item.files, context))
        holder.recyclerView.adapter = adapters[position]

        holder.addFilesBtn.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.setAction(Intent.ACTION_GET_CONTENT)
            item = dataSet[position]
            adapter = adapters[position]
            resultLauncher.launch(intent)
        }

        holder.inputName.addTextChangedListener {
            dataSet[position].name = holder.inputName.text.toString()
            context.checkContinueBtn()
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    private val resultLauncher =
        context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data;
                if (data != null) {
                    data.clipData?.let { clipData ->
                        val count = clipData.itemCount
                        for (i in 0 until count) {
                            val imageUri = clipData.getItemAt(i).uri
                            val file = getFileFromUri(imageUri)
                            item.files.add(file)
                            adapter.notifyItemInserted(item.files.size - 1)
                        }
                    }
                    context.checkContinueBtn()
                }
            }
        }

    private fun getFileFromUri(uri: Uri): File {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return File(filePath)
    }

}