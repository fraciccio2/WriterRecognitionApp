package com.unict.inkrecognition.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.unict.inkrecognition.R
import com.unict.inkrecognition.models.TestResultResponse
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
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.modal_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val message: TextView = dialog.findViewById(R.id.modal_message)
            val yesBtn: Button = dialog.findViewById(R.id.yes_btn)
            val noBtn: Button = dialog.findViewById(R.id.no_btn)
            message.text = getString(R.string.analyze_images_message)
            yesBtn.setOnClickListener {
                val dialogNew = Dialog(this)
                dialogNew.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogNew.setCancelable(false)
                dialogNew.setContentView(R.layout.modal_result)
                dialogNew.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.dismiss()
                dialogNew.show()
                postRequest(dialogNew)
            }
            noBtn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        testImageView.setOnClickListener {
            val intent = Intent(this, ImageViewActivity::class.java)
            intent.putExtra(getString(R.string.file_path_key), fileTest.path)
            startActivity(intent)
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
                        loadDataBtn.setEnabled(true)
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

    private fun postRequest(dialog: Dialog) {
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
                val responseBody = response.body!!.string()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                }

                responseBody.let {
                    val progressBar: ProgressBar = dialog.findViewById(R.id.progress_bar)
                    val layoutResult: LinearLayout = dialog.findViewById(R.id.layout_result)
                    val result: TextView = dialog.findViewById(R.id.result_name)
                    val resultAccuracy: TextView = dialog.findViewById(R.id.result_accuracy)
                    val accuracyLabel: TextView = dialog.findViewById(R.id.accuracy_label)
                    val homeBtn: Button = dialog.findViewById(R.id.return_home_btn)
                    homeBtn.setOnClickListener {
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)

                    }
                    try {
                        val mapper = jacksonObjectMapper().registerKotlinModule()
                        val state: TestResultResponse =
                            mapper.readValue(responseBody, TestResultResponse::class.java)

                        runOnUiThread {
                            result.text = state.results.firstOrNull() ?: "Nessun risultato"
                            val accuracy: Double = state.accuracies.firstOrNull() ?: 0.0
                            val accuracyFormat = DecimalFormat("##.##")
                            val formattedAccuracy = accuracyFormat.format(accuracy*10)+"%"
                            resultAccuracy.text = formattedAccuracy
                            layoutResult.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            result.text = e.message
                            layoutResult.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                            accuracyLabel.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        })
    }

    companion object {
        val MEDIA_TYPE_MARKDOWN = "image/*".toMediaType()
    }
}