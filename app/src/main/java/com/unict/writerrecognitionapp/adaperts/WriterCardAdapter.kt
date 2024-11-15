package com.unict.writerrecognitionapp.adaperts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unict.writerrecognitionapp.R
import com.unict.writerrecognitionapp.activities.MainActivity
import com.unict.writerrecognitionapp.holders.WriterCardHolder
import com.unict.writerrecognitionapp.models.Writer
import java.io.File
import java.util.Date


class WriterCardAdapter(private val dataSet: ArrayList<Writer>, private val context: MainActivity) :
    RecyclerView.Adapter<WriterCardHolder>() {
    private lateinit var item: Writer
    private var adapters: ArrayList<FileListAdapter> = arrayListOf()
    private lateinit var adapter: FileListAdapter
    private lateinit var currentPhotoPath: String

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WriterCardHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.writer_card, parent, false)
        return WriterCardHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: WriterCardHolder, position: Int) {
        item = dataSet[position]

        holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        adapters.add(FileListAdapter(item.files, context))
        holder.recyclerView.adapter = adapters[position]

        holder.addFilesGalleryBtn.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.setAction(Intent.ACTION_GET_CONTENT)
            item = dataSet[position]
            adapter = adapters[position]
            resultLauncher.launch(intent)
        }

        holder.addFilesCameraBtn.setOnClickListener {
            item = dataSet[position]
            adapter = adapters[position]
            val photoFile: File = createImageFile()
            val photoUri: Uri = FileProvider.getUriForFile(
                context,
                "com.unict.writerrecognitionapp.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        }

        holder.inputName.addTextChangedListener {
            dataSet[position].name = holder.inputName.text.toString()
            context.checkContinueBtn()
        }

        holder.recyclerView.setOnTouchListener { _, event ->
            val fatherRecyclerView = context.findViewById<RecyclerView>(R.id.card_writer)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    fatherRecyclerView.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    fatherRecyclerView.requestDisallowInterceptTouchEvent(false)
                }
            }
            return@setOnTouchListener false
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
                    } ?: data.data?.let { singleImageUri ->
                        val file = getFileFromUri(singleImageUri)
                        item.files.add(file)
                        adapter.notifyItemInserted(item.files.size - 1)
                    }
                    context.checkContinueBtn()
                }
            }
        }

    private val takePictureLauncher =
        context.registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val photoFile = File(currentPhotoPath)
                item.files.add(photoFile)
                adapter.notifyItemInserted(item.files.size - 1)
                context.checkContinueBtn()
            }
        }

    private fun getFileFromUri(uri: Uri): File {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "tmp"
        val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalArgumentException("Impossibile accedere al file da URI: $uri")
        return tempFile
    }

    private fun createImageFile(): File {
        val timeStamp = Date().time
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, "$timeStamp.jpg")
        currentPhotoPath = file.absolutePath
        return file
    }

}