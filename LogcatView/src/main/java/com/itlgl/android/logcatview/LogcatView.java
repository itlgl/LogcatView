package com.itlgl.android.logcatview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 将logcat的日志输出到TextView的工具，可以直接在手机上查看log
 *
 * 写完测试发现一个头疼的问题，当滑动LogcatView内的ScrollView时，系统会打出onTouchEvent的一堆日志，导致控件易用性很差
 * 所以稍微修改了一下后提供了另外一个LogcatTagView，仅显示指定Tag("LogcatView")的内容，剔除无用日志
 */
public class LogcatView extends FrameLayout {
    private static final String[] LEVEL_STRING = new String[]{"V", "D", "I", "W", "E"};
    private static final String[] LEVEL_LOG_COLOR_CLASSIC = new String[]{"BBBBBB", "0070BB", "48BB31", "BBBB23", "FF0006"};
    private static final String[] LEVEL_LOG_COLOR_INTELLIJ = new String[]{"000000", "000000", "000000", "00007F", "7F0000"};

    private View btnAutoScroll;
    private View btnClearLog;
    private View btnAutoScrollBg;
    private AppCompatSpinner spinner;
    private TextView tvLog;
    private ScrollView scrollView;

    private boolean autoScroll = true;
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };
    private Handler logAppendHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg != null && msg.obj != null) {
                appendLogcatText((String) msg.obj);
            }
        }
    };
    private Runnable logReadRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream(), Charset.forName("utf-8")));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    logAppendHandler.obtainMessage(1, line).sendToTarget();
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    };
    private String[] logcatColor = LEVEL_LOG_COLOR_CLASSIC;
    private int spinnerSelectedPositionBefore = 0;

    private Process logcatProcess = null;


    public LogcatView(Context context) {
        super(context);
        init();
    }

    public LogcatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initView();
        initLogcatCmd();
    }

    private void initView() {
        inflate(getContext(), R.layout.logcatviewlib_layout_logcatview, this);
        btnAutoScroll = findViewById(R.id.btn_auto_scroll);
        btnClearLog = findViewById(R.id.btn_clear_log);
        btnAutoScrollBg = findViewById(R.id.btn_auto_scroll_bg);
        spinner = findViewById(R.id.spinner);
        scrollView = findViewById(R.id.scroll_view);
        tvLog = findViewById(R.id.tv_log);

        btnAutoScrollBg.setBackgroundResource(autoScroll ? R.drawable.logcatviewlib_shape_selected_bg : 0);
        btnAutoScroll.setOnClickListener(v -> {
            autoScroll = !autoScroll;
            btnAutoScrollBg.setBackgroundResource(autoScroll ? R.drawable.logcatviewlib_shape_selected_bg : 0);
        });

        btnClearLog.setOnClickListener(v -> {
            clearLogcat();
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != spinnerSelectedPositionBefore) {
                    spinnerSelectedPositionBefore = position;
                    initLogcatCmd();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private synchronized void initLogcatCmd() {
        if (logcatProcess != null) {
            logcatProcess.destroy();
            logcatProcess = null;
        }
        tvLog.setText("");
        String cmd = null;
        String[] customFilterTags = customFilterTags();
        int pid = android.os.Process.myPid();
        String level = LEVEL_STRING[spinner.getSelectedItemPosition()];
        if(customFilterTags == null) {
            cmd = String.format("logcat *:%s --pid %s -v time", level, pid);
        } else {
            // adb logcat ActivityManager:I MyApp:D *:S
            // 最后缀上*:S可以使除指定tag外的所有log都被过滤掉
            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append("logcat");
            for (String tag : customFilterTags) {
                cmdBuilder.append(" ").append(tag).append(":").append(level);
            }
            cmdBuilder.append(" *:S --pid ").append(pid).append(" -v time");
            cmd = cmdBuilder.toString();
        }
        try {
            logcatProcess = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            //e.printStackTrace();
            appendLogcatText("\n" + e);
        }
        if (logcatProcess == null) {
            // init logcat process error
            appendLogcatText("\ninit logcat process error");
            return;
        }
        new Thread(logReadRunnable).start();
    }

    public String[] customFilterTags() {
        return null;
    }

    public void clearLogcat() {
        btnClearLog.setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    Process exec = Runtime.getRuntime().exec("logcat -c");
                    exec.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                logAppendHandler.post(() -> {
                    tvLog.setText("");
                    btnClearLog.setEnabled(true);
                });
            }
        }.start();
    }

    private void appendLogcatText(String log) {
        if (TextUtils.isEmpty(log)) {
            return;
        }
        tvLog.append("\n");

        String color = logcatColor[0];
        // 12-28 14:26:07.283 W/InputMethodManager( 4662): startInputReason = 1
        if (log.length() >= 20) {
            switch (log.charAt(19)) {
                case 'V':
                    color = logcatColor[0];
                    break;
                case 'D':
                    color = logcatColor[1];
                    break;
                case 'I':
                    color = logcatColor[2];
                    break;
                case 'W':
                    color = logcatColor[3];
                    break;
                case 'E':
                    color = logcatColor[4];
                    break;
            }
        }
        String html = String.format("<font color=\"#%s\">%s</font>", color, log);
        tvLog.append(Html.fromHtml(html));

        if (autoScroll) {
            scrollView.post(autoScrollRunnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (logcatProcess != null) {
            logcatProcess.destroy();
            logcatProcess = null;
        }
    }


}
