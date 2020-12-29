package com.itlgl.android.logcatview.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_logcat_custom_cmd.*

class LogcatCustomCmdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logcat_custom_cmd)
        btnAddLog.setOnClickListener {
            println("this is a sample log")
        }
    }
}