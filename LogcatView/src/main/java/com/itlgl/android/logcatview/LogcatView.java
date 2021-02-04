package com.itlgl.android.logcatview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
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
 * 将logcat的日志输出到TextView的工具View，可以直接在手机上查看log
 *
 * <pre>
 * 1.在布局文件中，直接引用此view即可使用
 * <pre>
 * &lt;com.itlgl.android.logcatview.LogcatView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" &gt;
 * </pre>
 * <br></br>默认情况下logcat的语句为：logcat *:V --pid xx -v time
 * <br></br>在有的手机上，滑动ScrollView会产生系统log，这就导致只要一滑动，就产生log，自己的log不好找，可以通过过滤自定义tag的方式避免
 * <br></br><br></br><br></br>
 * 2.支持自定义过滤tag
 * <pre>
 * &lt;com.itlgl.android.logcatview.LogcatView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:filterTags="tag1,tag2,tag3" &gt;
 * </pre>
 * <br></br>过滤tag最后组装的cmd语句是这样：logcat tag1:V tag2:V tag3:V *:S --pid xx -v time
 * <br></br>这时
 * <br></br><br></br><br></br>
 * 3.支持自定义logcat语句
 * <pre>
 * &lt;com.itlgl.android.logcatview.LogcatView
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:customCmd="logcat -v time" &gt;
 * </pre>
 * <br></br>自定义logcat语句，请在cmd最后加上 -v time的格式，否则代码不能正确判断每一条log的优先级，导致log显示的颜色不正确
 * <br></br><br></br><br></br>
 * 4. 优先级问题
 * <br></br>customCmd > filterTags > default
 * <br></br>当customCmd不为空时，filterTags不起作用，默认cmd不起作用
 * <br></br>当customCmd为空时，filterTags才能起作用
 * <br></br>只有customCmd和filterTags都为空时，默认的cmd才会起作用
 *
 */
public class LogcatView extends FrameLayout {
    private static final String[] LEVEL_STRING = new String[]{"V", "D", "I", "W", "E"};
    private static final int[] LEVEL_LOG_COLOR_CLASSIC = new int[]{0xFFBBBBBB, 0xFF0070BB, 0xFF48BB31, 0xFFBBBB23, 0xFFFF0006};
    private static final int[] LEVEL_LOG_COLOR_INTELLIJ = new int[]{0x000000, 0x000000, 0x000000, 0x00007F, 0x7F0000};

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
                logAppendHandler.post(() -> {
                    appendErrorText("Logcat process exit!");
                });
            } catch (Exception e) {
                // e.printStackTrace();

                // 理想情况是logcat -c执行后，如果进程EOF读不出来数据，那么终止，并且显示错误信息
                // 实际情况是bufferedReader.readLine()依然在阻塞状态
                // 等到重新调用initLogcatCmd方法时，这里才收到一个被打断的异常，不是想要的结果
//                logAppendHandler.post(() -> {
//                    appendErrorText("Logcat process read error: " + (e != null ? e.getMessage() : "unknown"));
//                });
            }
        }
    };
    private int[] logcatColor = LEVEL_LOG_COLOR_CLASSIC;
    private int spinnerSelectedPositionBefore = 0;
    private String filterTags = null;
    private String customCmd = null;

    private Process logcatProcess = null;


    public LogcatView(Context context) {
        super(context);
        init();
    }

    public LogcatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LogcatView);
        filterTags = ta.getString(R.styleable.LogcatView_filterTags);
        customCmd = ta.getString(R.styleable.LogcatView_customCmd);
        ta.recycle();

        init();
    }

    public LogcatView(Context context, @Nullable String filterTags, @Nullable String customCmd) {
        super(context);
        this.filterTags = filterTags;
        this.customCmd = customCmd;
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
        if(isInEditMode()) {
            appendLogcatText("log will show here...");
            return;
        }

        if (logcatProcess != null) {
            try {
                logcatProcess.getInputStream().close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                logcatProcess.getErrorStream().close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                logcatProcess.getOutputStream().close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            logcatProcess.destroy();
            logcatProcess = null;
        }
        tvLog.setText("");

        String cmd = null;
        int pid = android.os.Process.myPid();
        String level = LEVEL_STRING[spinner.getSelectedItemPosition()];
        // 首先判断是否有自定义cmd，优先级最高
        if(!TextUtils.isEmpty(customCmd)) {
            cmd = customCmd;
        } else if(!TextUtils.isEmpty(filterTags)) { // 其次判断是否有自定义过滤tag
            String[] customFilterTags = filterTags.split(",");
            // adb logcat ActivityManager:I MyApp:D *:S
            // 最后缀上*:S可以使除指定tag外的所有log都被过滤掉
            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append("logcat");
            for (String tag : customFilterTags) {
                cmdBuilder.append(" ").append(tag).append(":").append(level);
            }
            cmdBuilder.append(" *:S --pid ").append(pid).append(" -v time");
            cmd = cmdBuilder.toString();
        } else {// 最后才是默认情况
            cmd = String.format("logcat *:%s --pid %s -v time", level, pid);
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

    public void clearLogcat() {
        btnClearLog.setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                int exit = -1;
                // 在特定设备上，执行logcat -c会报错"failed to clear the 'main' log"，并返回错误码1
                // 并且在执行完后，其他logcat的process会被中断，所以中断后需要在log上提示一下
                // 所以多执行几次，就会把log清除完成
                for (int i = 0; i < 5; i++) {
                    try {
//                    ProcessBuilder processBuilder = new ProcessBuilder("logcat", "-c");
//                    Process exec = processBuilder.start();
                        Process exec = Runtime.getRuntime().exec("logcat -c");
                        exit = exec.waitFor();
                    } catch (Exception e) {
                        //e.printStackTrace();
                        exit = -1;
                    }

                    if(exit == 0) {
                        break;
                    }
                }

                final int finalExit = exit;
                logAppendHandler.post(() -> {
                    btnClearLog.setEnabled(true);
                    if(finalExit == 0) {
                        tvLog.setText("");
                    } else {
                        tvLog.append("\ncan not clear log,error=" + finalExit);
                    }
                });
            }
        }.start();
    }

    private void appendLogcatText(String log) {
        if (TextUtils.isEmpty(log)) {
            return;
        }
        tvLog.append("\n");

        int color = logcatColor[0];
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

        // 不使用Html.fromHtml(html)的方法，因为当文字中含有特定字符（比如 "->"）时，会导致后面内容不显示
        // 如果log内有\t制表符，带颜色以后textView会崩溃
        log = log.replaceAll("\t", "    ");
        SpannableString ss = new SpannableString(log);
        ss.setSpan(new ForegroundColorSpan(color), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLog.append(ss);

        if (autoScroll) {
            scrollView.post(autoScrollRunnable);
        }
    }

    private void appendErrorText(String error) {
        tvLog.append("\n");

        error = error.replaceAll("\t", "    ");
        SpannableString ss = new SpannableString(error);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new BackgroundColorSpan(Color.RED), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLog.append(ss);

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
