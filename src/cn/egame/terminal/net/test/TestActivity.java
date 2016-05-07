/*
 * FileName:	TestActivity.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import cn.egame.terminal.net.FastTube;
import cn.egame.terminal.net.R;
import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;
import cn.egame.terminal.net.utils.Logger;
import cn.egame.terminal.utils.StorageAccess;

/**
 * </Br> <功能详细描述> </Br>
 *
 * @author Hein
 * @hide
 */
public class TestActivity extends Activity {

    public static final int LOGGER = 0;

    private final String testUrl1 = "http://open.play.cn/api/v2/mobile/channel/content.json?channel_id=701&terminal_id=245&current_page=0&rows_of_page=20&order_id=0";

    private final String fetchHostsUrl = "http://open.play.cn:80/api/v2/egame/host.json";

    private FastTube mFastTube = null;

    private static final TubeOptions NORMAL_OPTIONS = new TubeOptions.Builder()
            .setHostKey("TEST").setReconnectionTimes(10).create();

    private TextView mLoggerView = null;
    private ScrollView mScrollView = null;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case LOGGER:
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
            default:
                break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Logger.register(mHandler, LOGGER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFastTube = FastTube.getInstance();
        mFastTube.init(new TubeConfig.Builder().setThreadCount(10).create());

        mLoggerView = (TextView) findViewById(R.id.logout);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        findViewById(R.id.start).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doTask(true);
            }
        });

        findViewById(R.id.step1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doTask(false);
            }
        });

        findViewById(R.id.step2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                testFetchData();
            }
        });

        findViewById(R.id.step3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoggerView.setText("");
            }
        });

        Logger.v("LOGGER", "Ready!");
        Logger.d("LOGGER", "Ready!");
        Logger.i("LOGGER", "Ready!");
        Logger.w("LOGGER", "Ready!");
        Logger.e("LOGGER", "Ready!");

        StorageAccess.isStoreSufficient(0);
    }

    private void doTask(final boolean isAutoFetchData) {
        mFastTube.getJSON(fetchHostsUrl,
                new JSONTubeListener<LinkedList<String>>() {

                    @Override
                    public LinkedList<String> doInBackground(JSONObject water) {
                        // TODO Auto-generated method stub
                        return processData(water);
                    }

                    @Override
                    public void onSuccess(LinkedList<String> result) {
                        // TODO Auto-generated method stub
                        if (result != null) {

                            mFastTube.addHosts("TEST", result);

                            if (isAutoFetchData) {
                                testFetchData();
                            }
                        }
                    }

                    @Override
                    public void onFailed(TubeException e) {
                        // TODO Auto-generated method stub
                        Logger.e("wei.han", e.getLocalizedMessage());
                    }
                });
    }

    private LinkedList<String> processData(JSONObject water) {
        LinkedList<String> hosts = new LinkedList<String>();
        try {
            JSONArray array = water.getJSONObject("ext").getJSONArray(
                    "host_url");
            for (int i = 0; i < array.length(); i++) {
                hosts.add(array.getString(i));
                Logger.d("wei.han", "Host" + i + ": " + array.getString(i));
            }
            return hosts;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    private void testFetchData() {

        // ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        // pairs.add(new BasicNameValuePair("is", "me"));
        // TubeOptions opt = new
        // TubeOptions.Builder().setPostEntity(pairs).create();
        // mFastTube.post(testUrl1, opt, null);
        new Thread() {
            public void run() {
                mFastTube.getString(testUrl1, NORMAL_OPTIONS,
                        new StringTubeListener<JSONObject>() {

                            @Override
                            public JSONObject doInBackground(String water) {
                                // TODO Auto-generated method stub
                                try {
                                    return new JSONObject(water);
                                } catch (Exception e) {
                                    // TODO: handle exception
                                    return null;
                                }
                            }

                            @Override
                            public void onSuccess(JSONObject result) {
                                // TODO Auto-generated method stub
                                if (result != null) {
                                    Logger.d("wei.han", result.toString());
                                }
                            }

                            @Override
                            public void onFailed(TubeException e) {
                                // TODO Auto-generated method stub
                                Logger.e("wei.han", e.getLocalizedMessage());
                            }
                        });
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Logger.unRegister(mHandler);
        if (mFastTube != null) {
            mFastTube.release();
        }
    }

}
