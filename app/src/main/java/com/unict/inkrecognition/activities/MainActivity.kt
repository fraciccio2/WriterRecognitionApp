package com.unict.inkrecognition.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.unict.inkrecognition.R
import com.unict.inkrecognition.adaperts.WriterCardAdapter
import com.unict.inkrecognition.models.Writer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException


class MainActivity : ComponentActivity() {
    private lateinit var addWriterBtn: Button
    private lateinit var addTestFileBtn: Button
    private lateinit var loadDataBtn: Button
    private lateinit var testLayout: LinearLayout
    private lateinit var testTextView: TextView
    private lateinit var testImageView: ImageView
    private lateinit var fileTest: File
    private var writers: ArrayList<Writer> = arrayListOf(
        Writer(
            name = "",
            files = arrayListOf()
        ),
        Writer(
            name = "",
            files = arrayListOf()
        )
    )
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WriterCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        addWriterBtn = findViewById(R.id.add_writer_btn)
        addTestFileBtn = findViewById(R.id.test_btn)
        loadDataBtn = findViewById(R.id.load_data)
        testLayout = findViewById(R.id.test_layout)
        testTextView = findViewById(R.id.text_view_test)
        testImageView = findViewById(R.id.test_show_image)
        recyclerView = findViewById(R.id.card_writer)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WriterCardAdapter(writers, this)
        recyclerView.adapter = adapter

        addWriterBtn.setOnClickListener {
            writers.add(Writer(
                name = "",
                files = arrayListOf()
            ))
            adapter.notifyItemInserted(writers.size - 1)
            recyclerView.smoothScrollToPosition(writers.size - 1)
        }

        addTestFileBtn.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher.launch(intent)
        }

        loadDataBtn.setOnClickListener {
            postRequest()
        }

        testImageView.setOnClickListener {
            //TODO rivedere
            val mBuilder = AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
            val mAlertDialog = mBuilder.create()
            mAlertDialog.setContentView(R.layout.alert_image)
            val imageView: ImageView = mAlertDialog.findViewById(R.id.image_full_screen)
            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
            Glide.with(this).load(fileTest).apply(options).into(imageView)
            mAlertDialog.show()
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data;
            if(data != null) {
                val imageUri = data.data
                if(imageUri != null) {
                    fileTest = getFileFromUri(imageUri)
                    testLayout.visibility = View.VISIBLE
                    testTextView.setText(fileTest.name)
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return File(filePath)
    }

    private fun postRequest() {
        val client = OkHttpClient()
        val multipartBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        for(writer in writers) {
            for((i, file) in writer.files.withIndex()) {
                multipartBodyBuilder.addFormDataPart(
                    "${writer.name}$i",
                    file.name,
                    file.asRequestBody(MEDIA_TYPE_MARKDOWN)
                )
            }
        }

        multipartBodyBuilder.addFormDataPart(
            "test0",
            fileTest.name,
            fileTest.asRequestBody(MEDIA_TYPE_MARKDOWN)
        )

        val namesWriters = writers.joinToString(",") { w -> w.name }
        multipartBodyBuilder.addFormDataPart("writers", namesWriters)

        val requestBody = multipartBodyBuilder.build()

        val request = Request.Builder()
            .url("http://192.168.1.62:5000/files")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })
    }

    companion object {
        val MEDIA_TYPE_MARKDOWN = "image/*".toMediaType()
    }
}