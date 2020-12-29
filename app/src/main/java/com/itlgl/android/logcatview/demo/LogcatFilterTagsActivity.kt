package com.itlgl.android.logcatview.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_logcat_tag_view.*

class LogcatFilterTagsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logcat_tag_view)
        btnAddLog.setOnClickListener {
            Log.i("L", "this is a sample log")
        }
    }
}