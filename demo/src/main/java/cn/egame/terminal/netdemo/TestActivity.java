/*
 * FileName:	TestActivity.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.netdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import cn.egame.terminal.net.FastTube;
import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.BitmapTubeListener;
import cn.egame.terminal.net.listener.GsonModelTubeListener;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.RawResponseListener;
import cn.egame.terminal.netdemo.model.EgameHost;
import cn.egame.terminal.utils.ELog;
import cn.egame.terminal.utils.StorageAccess;

/**
 * </Br> <功能详细描述> </Br>
 *
 * @author Hein
 * @hide
 */
public class TestActivity extends Activity {

    static {
        Logger.init();
    }

    public static final int LOGGER = 0;

    private final String testUrl1 = "http://open.play.cn/api/v2/mobile/channel/content.json?channel_id=701&terminal_id=245&current_page=0&rows_of_page=20&order_id=0";

//    private final String fetchHostsUrl = "http://open.play.cn:80/api/v2/egame/host.json";
    private final String fetchHostsUrl = "http://www.baidu.com";
    private final String imageUrl = "https://gss0.bdstatic.com/5bd1bjqh_Q23odCf/static/wiseindex/img/fetch_finish_8_0.png";

    private FastTube mFastTube = null;

    private static final TubeOptions NORMAL_OPTIONS = new TubeOptions.Builder()
            .setHostKey("TEST").setReconnectionTimes(10).create();

    private TextView mLoggerView = null;
    private ScrollView mScrollView = null;

    private ImageView ivImage;

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

        ELog.register(mHandler, LOGGER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        CrashHandler.getInstance().init(this);

        mFastTube = FastTube.getInstance();
        mFastTube.init(new TubeConfig.Builder().create());

        mLoggerView = (TextView) findViewById(R.id.logout);
        mLoggerView.setAutoLinkMask(Linkify.WEB_URLS);
        mLoggerView.setMovementMethod(LinkMovementMethod.getInstance());
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        ivImage = (ImageView) findViewById(R.id.image);

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
            public void onClick(View v) {
                getImage();
            }
        });

        ELog.v("LOGGER", "Ready!");
        ELog.d("LOGGER", "Ready!");
        ELog.i("LOGGER", "Ready!");
        ELog.w("LOGGER", "Ready!");
        ELog.e("LOGGER", "Ready!");

        // String url =
        // "http://a.hiphotos.baidu.com/image/pic/item/eaf81a4c510fd9f96737fc6e272dd42a2834a491.jpg";
        // FastTube.getInstance().getStream(url, new
        // StreamTubeListener<Bitmap>() {
        //
        // @Override
        // public Bitmap doInBackground(InputStream water) throws Exception {
        // // TODO Auto-generated method stub
        // return BitmapFactory.decodeStream(water);
        // }
        //
        // @Override
        // public void onSuccess(Bitmap result) {
        // // TODO Auto-generated method stub
        // findViewById(R.id.root_view).setBackgroundDrawable(
        // new BitmapDrawable(result));
        // // ViewGroup view = (ViewGroup) getWindow().getDecorView();
        // //
        // // ImageView iv = new ImageView(TestActivity.this);
        // // iv.setImageBitmap(result);
        // // ((ViewGroup) view.getChildAt(0)).addView(iv);
        // //
        // // ELog.d("wei.han",
        // // view.getClass().getSuperclass().getCanonicalName());
        //
        // // ((ViewGroup) view.getChildAt(0)).getChildAt(1)
        // // .setBackgroundDrawable(new BitmapDrawable(result));
        // }
        //
        // @Override
        // public void onFailed(TubeException e) {
        // // TODO Auto-generated method stub
        //
        // }
        // });

        StorageAccess.isStoreSufficient(0);
    }

    private void doTask(final boolean isAutoFetchData) {
        mFastTube.getString("http://open.play.cn/api/v2/egame/host.json", new GsonModelTubeListener<EgameHost>() {
            @Override
            public void onSuccess(EgameHost host) {
                ELog.e("Result", "host: " + host.ext);
            }

            @Override
            public void onFailed(TubeException e) {
                e.printStackTrace();

            }
        });
//        mFastTube.getString(fetchHostsUrl, new StringTubeListener<String>() {
//            @Override
//            public String doInBackground(String water) throws Exception {
//                return water;
//            }
//
//            @Override
//            public void onSuccess(String s) {
//                ELog.e("Result", s);
//
//            }
//
//            @Override
//            public void onFailed(TubeException e) {
//
//            }
//        });

/*
        mFastTube.getJSON(fetchHostsUrl, new JSONTubeListener<LinkedList<String>>() {

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
                        ELog.e("wei.han", e.getLocalizedMessage());
                    }
                });
*/
    }

    private LinkedList<String> processData(JSONObject water) {
        LinkedList<String> hosts = new LinkedList<String>();
        try {
            JSONArray array = water.getJSONObject("ext").getJSONArray(
                    "host_url");
            for (int i = 0; i < array.length(); i++) {
                hosts.add(array.getString(i));
                ELog.d("wei.han", "Host" + i + ": " + array.getString(i));
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
        // TubeOptions.Builder().setPostEntity(pairs).createBy();
        // mFastTube.post(testUrl1, opt, null);
        new Thread() {
            public void run() {
                mFastTube.getJSON(testUrl1, NORMAL_OPTIONS,
                        new JSONTubeListener<JSONObject>() {

                            @Override
                            public JSONObject doInBackground(JSONObject water) {
                                // TODO Auto-generated method stub
                                return water;
                            }

                            @Override
                            public void onSuccess(JSONObject result) {
                                // TODO Auto-generated method stub
//                                if (result != null) {
//                                    ELog.d("wei.han", result.toString());
//                                }
                            }

                            @Override
                            public void onFailed(TubeException e) {
                                // TODO Auto-generated method stub
                                ELog.e("wei.han", e.getLocalizedMessage());
                            }
                        });
            }
        }.start();

    }

    private void getImage() {
        mFastTube.getStream(imageUrl, null, new BitmapTubeListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                ivImage.setImageBitmap(bitmap);
                ivImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailed(TubeException e) {

            }
        });

        mFastTube.get(imageUrl, null, new RawResponseListener() {
            @Override
            public void onSuccess(TubeResponse tubeResponse) {

            }

            @Override
            public void onFailed(TubeException e) {

            }
        });

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
