package com.itlgl.android.logcatview.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.itlgl.android.logcatview.LogcatView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        btnDefault.setOnClickListener {
            startActivity(Intent(this, LogcatActivity::class.java))
        }
        btnFilterTag.setOnClickListener {
            startActivity(Intent(this, LogcatFilterTagsActivity::class.java))
        }
        btnCustomCmd.setOnClickListener {
            startActivity(Intent(this, LogcatCustomCmdActivity::class.java))
        }
        btnDialog.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("dialog demo")
                .setView(LogcatView(this, "L", null))
                .setPositiveButton("OK", null)
                .show()
        }
    }
}