package com.itlgl.android.logcatview.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.itlgl.android.logcatview.LogcatTagView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        btnLogcat.setOnClickListener {
            startActivity(Intent(this, LogcatActivity::class.java))
        }
        btnLogcatTag.setOnClickListener {
            startActivity(Intent(this, LogcatTagViewActivity::class.java))
        }
        btnLogcatDialog.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("LogcatTagView dialog demo")
                .setView(LogcatTagView(this))
                .setPositiveButton("OK", null)
                .show()
        }
    }
}