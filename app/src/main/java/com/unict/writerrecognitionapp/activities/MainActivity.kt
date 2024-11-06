package com.unict.writerrecognitionapp.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unict.writerrecognitionapp.R
import com.unict.writerrecognitionapp.adaperts.WriterCardAdapter
import com.unict.writerrecognitionapp.models.Writer
import com.unict.writerrecognitionapp.objects.WritersObject


class MainActivity : ComponentActivity() {
    private lateinit var continueBtn: MenuItem
    private lateinit var bottomNavigationView: BottomNavigationView
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

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        continueBtn = bottomNavigationView.menu.findItem(R.id.continue_btn)
        recyclerView = findViewById(R.id.card_writer)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WriterCardAdapter(writers, this)
        recyclerView.adapter = adapter

        bottomNavigationView.background = null
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.add_writer_btn -> {
                    writers.add(
                        Writer(
                            name = "",
                            files = arrayListOf()
                        )
                    )
                    adapter.notifyItemInserted(writers.size - 1)
                    recyclerView.smoothScrollToPosition(writers.size - 1)
                    continueBtn.setVisible(false)
                    true
                }
                R.id.continue_btn -> {
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
                    true
                }
                R.id.reload -> {
                    continueBtn.setVisible(false)
                    this.recreate()
                    true
                }

                else -> false
            }
        }
    }

    fun checkContinueBtn() {
        continueBtn.setVisible(writers.all { writer -> writer.name.isNotEmpty() && writer.files.size >= 2 })
    }
}