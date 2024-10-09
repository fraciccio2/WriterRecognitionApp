package com.unict.inkrecognition.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.unict.inkrecognition.R
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
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private var files1: ArrayList<File> = arrayListOf()
    private var files2: ArrayList<File> = arrayListOf()
    private var currentArray: MutableList<File>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        btn1 = findViewById(R.id.button1)
        btn2 = findViewById(R.id.button2)
        btn3 = findViewById(R.id.button3)

        btn1.setOnClickListener {
            pickImageGallery(files1)
        }

        btn2.setOnClickListener {
            pickImageGallery(files2)
        }

        btn3.setOnClickListener {
            postRequest()
        }
    }

    private fun pickImageGallery(array: MutableList<File>) {
        currentArray = array
        val intent = Intent()
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setAction(Intent.ACTION_GET_CONTENT)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data;
            if(data != null) {
                data.clipData?.let { clipData ->
                    val count = clipData.itemCount
                    for (i in 0 until count) {
                        val imageUri = clipData.getItemAt(i).uri
                        val file = getFileFromUri(imageUri)
                        currentArray?.add(file)
                    }
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

        for((i, f) in files1.withIndex()) {
            multipartBodyBuilder.addFormDataPart("f$i", f.name, f.asRequestBody(MEDIA_TYPE_MARKDOWN))
        }

        for((y, f) in files2.withIndex()) {
            multipartBodyBuilder.addFormDataPart("ff$y", f.name, f.asRequestBody(MEDIA_TYPE_MARKDOWN))
        }

        val requestBody = multipartBodyBuilder.build()

        val request = Request.Builder()
            .url("https://api.imgur.com/3/image")
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