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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.unict.inkrecognition.R
import com.unict.inkrecognition.models.Writer
import com.unict.inkrecognition.objects.WritersObject
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

class TestActivity : AppCompatActivity() {
    private lateinit var addTestFileBtn: Button
    private lateinit var loadDataBtn: Button
    private lateinit var testLayout: LinearLayout
    private lateinit var testTextView: TextView
    private lateinit var testImageView: ImageView
    private lateinit var fileTest: File
    private lateinit var writers: ArrayList<Writer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        writers = WritersObject.getWriters()

        addTestFileBtn = findViewById(R.id.test_btn)
        loadDataBtn = findViewById(R.id.load_data)
        testLayout = findViewById(R.id.test_layout)
        testTextView = findViewById(R.id.text_view_test)
        testImageView = findViewById(R.id.test_show_image)

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
            val mBuilder = AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
            )
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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data;
                if (data != null) {
                    val imageUri = data.data
                    if (imageUri != null) {
                        fileTest = getFileFromUri(imageUri)
                        testLayout.visibility = View.VISIBLE
                        testTextView.text = fileTest.name
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

        for (writer in writers) {
            for ((i, file) in writer.files.withIndex()) {
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
            .url("http://75.119.130.105:5000/files")
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