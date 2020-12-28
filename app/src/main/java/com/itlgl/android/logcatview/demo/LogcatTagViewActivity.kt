package com.itlgl.android.logcatview.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.itlgl.android.logcatview.LogcatTagView
import kotlinx.android.synthetic.main.activity_logcat_tag_view.*

class LogcatTagViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logcat_tag_view)
        btnAddLog.setOnClickListener {
            Log.i(LogcatTagView.TAG, "this is a sample log")
        }
    }
}