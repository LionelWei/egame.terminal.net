package cn.egame.terminal.net.core;


import android.os.Handler;
import android.os.Looper;

import okhttp3.OkHttpClient;

public class OkHttpClientManager {
    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;


    private static final String TAG = "OkHttpClientManager";

    private OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient();
        //cookie enabled
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }
}