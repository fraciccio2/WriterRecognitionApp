package com.unict.writerrecognitionapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unict.writerrecognitionapp.R
import com.unict.writerrecognitionapp.models.TestResultResponse
import com.unict.writerrecognitionapp.models.Writer
import com.unict.writerrecognitionapp.objects.WritersObject
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
import java.util.Date

class TestActivity : AppCompatActivity() {
    private lateinit var loadDataItem: MenuItem
    private lateinit var testImageView: ImageView
    private lateinit var writers: ArrayList<Writer>
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var currentPhotoPath: String
    private var fileTest: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        writers = WritersObject.getWriters()

        testImageView = findViewById(R.id.test_image_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        loadDataItem = bottomNavigationView.menu.findItem(R.id.load_data)

        bottomNavigationView.background = null
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.reload_test -> {
                    fileTest = null
                    testImageView.setImageDrawable(null)
                    loadDataItem.setVisible(false)
                    true
                }
                R.id.test_btn -> {
                    val intent = Intent()
                    intent.setType("image/*")
                    intent.setAction(Intent.ACTION_GET_CONTENT)
                    resultLauncher.launch(intent)
                    true
                }
                R.id.test_photo_btn -> {
                    val photoFile: File = createImageFile()
                    val photoUri: Uri = FileProvider.getUriForFile(
                        this,
                        "com.unict.writerrecognitionapp.fileprovider",
                        photoFile
                    )
                    takePictureLauncher.launch(photoUri)
                    loadDataItem.setVisible(true)
                    true
                }
                R.id.load_data -> {
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
                        val displayMetrics = DisplayMetrics()
                        windowManager.defaultDisplay.getMetrics(displayMetrics)
                        dialogNew.window?.setLayout(displayMetrics.widthPixels, WindowManager.LayoutParams.WRAP_CONTENT)
                        dialog.dismiss()
                        dialogNew.show()
                        postRequest(dialogNew)
                    }
                    noBtn.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.show()
                    true
                }

                else -> false
            }
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
                        loadDataItem.setVisible(true)
                        testImageView.setImageURI(imageUri)
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
            fileTest!!.name,
            fileTest!!.asRequestBody(MEDIA_TYPE_MARKDOWN)
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
                val result: TextView = dialog.findViewById(R.id.result_name)
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            val progressBar: ProgressBar = dialog.findViewById(R.id.progress_bar)
                            val layoutResult: LinearLayout = dialog.findViewById(R.id.layout_result)
                            val errorMessage: TextView = dialog.findViewById(R.id.error_message)
                            val homeBtn: Button = dialog.findViewById(R.id.return_home_btn)
                            val writerLabel: TextView = dialog.findViewById(R.id.writer_label)
                            val accuracyLabel: TextView = dialog.findViewById(R.id.accuracy_label)
                            val resultAccuracy: TextView = dialog.findViewById(R.id.result_accuracy)
                            progressBar.visibility = View.INVISIBLE
                            layoutResult.visibility = View.VISIBLE
                            homeBtn.visibility = View.VISIBLE
                            errorMessage.text = getString(R.string.error_500)
                            errorMessage.visibility = View.VISIBLE
                            homeBtn.setOnClickListener {
                                val intent = Intent(baseContext, MainActivity::class.java)
                                startActivity(intent)

                            }
                            writerLabel.visibility = View.GONE
                            result.visibility = View.GONE
                            accuracyLabel.visibility = View.GONE
                            resultAccuracy.visibility = View.GONE
                        }
                        throw IOException("Unexpected code $response")
                    }
                }

                responseBody.let {
                    val progressBar: ProgressBar = dialog.findViewById(R.id.progress_bar)
                    val layoutResult: LinearLayout = dialog.findViewById(R.id.layout_result)
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
                            val accuracyPercentage = accuracy * 100
                            val formattedAccuracy = "%.2f%%".format(accuracyPercentage)
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

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val photoFile = File(currentPhotoPath)
                fileTest = photoFile
                testImageView.setImageURI(Uri.fromFile(fileTest))
            }
        }

    private fun createImageFile(): File {
        val timeStamp = Date().time
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, "$timeStamp.jpg")
        currentPhotoPath = file.absolutePath
        return file
    }

    companion object {
        val MEDIA_TYPE_MARKDOWN = "image/*".toMediaType()
    }
}