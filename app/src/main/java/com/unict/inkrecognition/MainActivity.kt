package com.unict.inkrecognition

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.unict.inkrecognition.StrokeManager.clear
import com.unict.inkrecognition.StrokeManager.download
import com.unict.inkrecognition.StrokeManager.recognize


class MainActivity : ComponentActivity() {
    private lateinit var btnRecognize: Button
    private lateinit var btnClear: Button
    private lateinit var drawView: DrawView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        btnRecognize = findViewById(R.id.buttonRecognize)
        btnClear = findViewById(R.id.buttonClear)
        drawView = findViewById(R.id.draw_view)
        textView = findViewById(R.id.textResult)

        download()
        btnRecognize.setOnClickListener {
            recognize(
                textView
            )
        }
        btnClear.setOnClickListener {
            drawView.clear()
            clear()
            textView.text = ""
        }

    }
}