package com.unict.writerrecognitionapp.activities

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.unict.writerrecognitionapp.R
import java.io.File

class ImageViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val imageView: ImageView = findViewById(R.id.image_view_preview)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        val filePath = intent.getStringExtra(getString(R.string.file_path_key))

        filePath?.let {
            val file = File(it)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            }
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}