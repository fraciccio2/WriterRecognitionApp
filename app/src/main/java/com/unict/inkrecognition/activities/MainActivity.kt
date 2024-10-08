package com.unict.inkrecognition.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.unict.inkrecognition.R


class MainActivity : ComponentActivity() {
    private lateinit var btn1: Button
    private lateinit var btn2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        btn1 = findViewById(R.id.button1)
        btn2 = findViewById(R.id.button2)

        btn1.setOnClickListener {
            pickImageGallery()
        }
    }

    private fun pickImageGallery() {
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
                    }
                } ?: run {
                    data?.data?.let { uri ->
                        val imagePath = uri.path
                    }
                }
            }
        }
    }
}