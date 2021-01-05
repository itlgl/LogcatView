package com.itlgl.android.logcatview.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_logcat.*

class LogcatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logcat)
        btnAddLog.setOnClickListener {
            try {
                val num = 1 / 0
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}