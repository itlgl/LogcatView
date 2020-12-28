package com.itlgl.android.logcatview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class LogcatTagView extends LogcatView {
    public static final String TAG = "L";

    public LogcatTagView(Context context) {
        super(context);
    }

    public LogcatTagView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String[] customFilterTags() {
        return new String[] {TAG};
    }
}
