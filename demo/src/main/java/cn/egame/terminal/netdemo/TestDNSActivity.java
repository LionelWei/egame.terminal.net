/*
 * FileName:	TestActivity.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.netdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import cn.egame.terminal.net.FastTube;
import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.utils.ELog;

/**
 * </Br> <功能详细描述> </Br>
 *
 * @author Hein
 * @hide
 */
public class TestDNSActivity extends Activity {

    private static final String TAG = "TAG";

    static {
        com.orhanobut.logger.Logger.init();
    }

    public static final int TEXT_APPEND = 0;
    public static final int TEXT_CLEAR = 1;
    public static final int DOMAIN_FORMAT_ERROR = 2;

    private final String mDnsUrl = "http://119.29.29.29/d?dn=";

    private String mTargetDomain = "cdn.4g.play.cn";

    private final String mLocalIPUrl = "http://pv.sohu.com/cityjson?ie=utf-8";

    private FastTube mFastTube = null;

    private static final TubeOptions NORMAL_OPTIONS = new TubeOptions.Builder()
            .setReconnectionTimes(0)
            .setHeaders(new HashMap<String, String>()).create();

    private EditText mURLEditText = null;
    private TextView mLoggerView = null;
    private ScrollView mScrollView = null;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case TEXT_APPEND:
                    if (mLoggerView == null) {
                        return;
                    }

                    mLoggerView.append((CharSequence) msg.obj);

                    mScrollView.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                    break;
                case TEXT_CLEAR:
                    if (mLoggerView == null) {
                        return;
                    }

                    mLoggerView.setText("");
                    break;
                case DOMAIN_FORMAT_ERROR:
                    Toast.makeText(TestDNSActivity.this,
                            "域名格式错误，请重新输入。", Toast.LENGTH_SHORT);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_dns);

        mFastTube = FastTube.getInstance();
        mFastTube.init(new TubeConfig.Builder().create());

        mURLEditText = (EditText) findViewById(R.id.url_et);
        mURLEditText.setText(mTargetDomain);
        mURLEditText.setSelection(mTargetDomain.length());
        mLoggerView = (TextView) findViewById(R.id.logout);
        mLoggerView.setAutoLinkMask(Linkify.WEB_URLS);
//        mLoggerView.setMovementMethod(LinkMovementMethod.getInstance());
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        final View btn = findViewById(R.id.start);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread() {
                    @Override
                    public void run() {
                        synchronized (TestDNSActivity.class) {
                            clearText();
                            fetchLocalIP();
                            fetchDNS();
                        }
                    }
                }.start();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.requestFocus();
            }
        }, 10);
    }

    private void clearText() {
        mHandler.sendEmptyMessage(TEXT_CLEAR);
    }

    private void fetchLocalIP() {
        String result = mFastTube.connectSync(mLocalIPUrl, NORMAL_OPTIONS);

        ELog.d(TAG, result);
        mHandler.sendMessage(mHandler.obtainMessage(TEXT_APPEND,
                "客户端IP: \n" + result + "\n\n"));
    }

    private void fetchDNS() {
        String domain = mURLEditText.getText().toString();

        if (TextUtils.isEmpty(domain)
                || domain.startsWith("http")
                || domain.endsWith("/")) {
            mHandler.sendEmptyMessage(DOMAIN_FORMAT_ERROR);
            return;
        }

        String result = mFastTube.connectSync(mDnsUrl + domain, NORMAL_OPTIONS);

        ELog.d(TAG, result);
        mHandler.sendMessage(mHandler.obtainMessage(TEXT_APPEND, "DNS解析主机列表: \n"
                + result.replaceAll(";", "\n")));
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ELog.unRegister(mHandler);
        if (mFastTube != null) {
            mFastTube.release();
        }
    }

}
