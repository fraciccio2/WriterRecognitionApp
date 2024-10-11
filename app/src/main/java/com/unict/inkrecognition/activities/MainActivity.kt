package com.unict.inkrecognition.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unict.inkrecognition.R
import com.unict.inkrecognition.adaperts.WriterCardAdapter
import com.unict.inkrecognition.models.Writer
import com.unict.inkrecognition.objects.WritersObject


class MainActivity : ComponentActivity() {
    private lateinit var addWriterBtn: Button
    private lateinit var continueBtn: Button
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
        setContentView(R.layout.activity_main)

        addWriterBtn = findViewById(R.id.add_writer_btn)
        continueBtn = findViewById(R.id.continue_btn)
        recyclerView = findViewById(R.id.card_writer)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WriterCardAdapter(writers, this)
        recyclerView.adapter = adapter

        addWriterBtn.setOnClickListener {
            writers.add(
                Writer(
                    name = "",
                    files = arrayListOf()
                )
            )
            adapter.notifyItemInserted(writers.size - 1)
            recyclerView.smoothScrollToPosition(writers.size - 1)
            continueBtn.setEnabled(false)
        }

        continueBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.modal_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val yesBtn: Button = dialog.findViewById(R.id.yes_btn)
            val noBtn: Button = dialog.findViewById(R.id.no_btn)

            yesBtn.setOnClickListener {
                val intent = Intent(this, TestActivity::class.java)
                WritersObject.setWriters(writers)
                startActivity(intent)
            }

            noBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    fun checkContinueBtn() {
        continueBtn.setEnabled(writers.all { writer -> writer.name.isNotEmpty() && writer.files.size >= 2 })
    }
}