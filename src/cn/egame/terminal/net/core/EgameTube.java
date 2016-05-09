/*
 * FileName:	EgameBox.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.StreamTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;
import cn.egame.terminal.net.listener.TubeListener;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 网络管道类 对于配置信息相同仅URL不同的请求可共用一个对象
 *
 * @author Hein
 */
public class EgameTube {

    private TubeThreadPool mTubePool = null;
    private TubeConfig mConfig = TubeConfig.getDefault();

    public EgameTube() {
    }

    public void init(TubeConfig cfg) {

        mConfig = cfg;

        if (mTubePool != null) {
            return;
        }

        if (mConfig.mThreadCount > 0) {
            mTubePool = TubeThreadPool.create(mConfig.mThreadCount);
        }

        // Logger.LOG_ON = mConfig.isDebug;
    }

    public void addHosts(String key, LinkedList<String> hosts) {
        if (mConfig != null) {
            mConfig.mHosts.put(key, hosts);
        }
    }

    public void putCommonHeader(String key, String value) {
        if (mConfig != null) {
            mConfig.mCommonHeaders.put(key, value);
        }
    }

    public void release() {
        if (mTubePool != null) {
            new Thread() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mTubePool.closePool();
                }

            }.start();
        }

        mConfig = null;
    }

    /**
     * 获取数据
     *
     * @param url      请求地址
     *                 需要切换的主机表Key
     * @param listener 数据返回
     */
    public void get(final String url, final TubeOptions opt,
                    final TubeListener<?, ?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener can not be null.");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        Looper myLooper = Looper.myLooper();

        // 如果本线程没有looper则使用主线程looper 有风险 wei.han 20131031
        if (myLooper == null) {
            myLooper = Looper.getMainLooper();
        }

        if (mTubePool == null) {
            new Thread(getRunnable(myLooper, url, opt, listener), "EgameTube:"
                    + hashCode()).start();
        } else {
            mTubePool.execute(getRunnable(myLooper, url, opt, listener));
        }
    }

    public String connect(final String url, final TubeOptions opt) {
        return connectString(url, opt);
    }

    public String connectString(final String url, final TubeOptions opt) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        return getString(null, url, opt, null);
    }

//    public void connectStream(final String url, final TubeOptions opt) {
//        try {
//            new URL(url);
//        } catch (MalformedURLException e) {
//            // TODO Auto-generated catch block
//            throw new IllegalArgumentException(
//                    "The url can not be parsed. Please check it again.");
//        }
//
//        return getStream(null, url, opt, null);
//    }

    private Runnable getRunnable(final Looper myLooper, final String url,
                                 final TubeOptions opt, final TubeListener<?, ?> listener) {

        return new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                // TODO Auto-generated method stub

                if (!listener.prepare(url, opt)) {
                    makeFailed(new Handler(myLooper), listener, new TubeException("Prepare failed."));
                    return;
                }

                if (listener instanceof StringTubeListener) {
                    getString(myLooper, url, opt,
                            (StringTubeListener<Object>) listener);
                } else if (listener instanceof JSONTubeListener) {
                    getJSON(myLooper, url, opt,
                            (JSONTubeListener<Object>) listener);
                } else if (listener instanceof StreamTubeListener) {
                    getStream(myLooper, url, opt,
                            (StreamTubeListener<Object>) listener);
                }
            }
        };
    }

    private String getString(Looper myLooper, String url, TubeOptions opt,
                             final StringTubeListener<Object> listener) {
        String result = null;

        Handler handler = null;

        // 这种条件说明是异步请求，需要异步返回结果
        if (myLooper != null && listener != null) {
            handler = new Handler(myLooper);
        }

        try {
            Response response = OkHttpConnection.okHttpExecute(url, mConfig, opt);
            result = OkHttpConnection.getString(response);
        } catch (TubeException e) {
            // TODO Auto-generated catch block
            if (listener != null) {
                makeFailed(handler, listener, e);
            }

            return null;
        }

        if (TextUtils.isEmpty(result)) {
            if (listener != null) {
                makeFailed(handler, listener, new TubeException(
                        "The result is null or empty."));
            }

            return null;
        }

        if (handler != null) {
            try {
                final Object object = listener.doInBackground(result);

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        listener.onSuccess(object);
                    }
                });
            } catch (Exception e) {
                makeFailed(handler, listener, new TubeException(e,
                        TubeException.DATA_ERROR_CODE));
            }
        }

        return result;
    }

    private void getJSON(Looper myLooper, String url, TubeOptions opt,
                         final JSONTubeListener<Object> listener) {
        JSONObject result = null;

        Handler handler = new Handler(myLooper);


        try {
            Response response = OkHttpConnection.okHttpExecute(url, mConfig, opt);
            result = new JSONObject(OkHttpConnection.getString(response));
        } catch (TubeException e) {
            // TODO Auto-generated catch block
            makeFailed(handler, listener, e);
            return;
        } catch (JSONException e) {
            makeFailed(handler, listener,
                    new TubeException(e.getLocalizedMessage(),
                            TubeException.DATA_ERROR_CODE));
            return;
        }

        try {
            final Object object = listener.doInBackground(result);
            handler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    listener.onSuccess(object);
                }
            });
        } catch (Exception e) {
            makeFailed(handler, listener,
                    new TubeException(e.getLocalizedMessage(),
                            TubeException.DATA_ERROR_CODE));
            return;
        }
    }

    private Response getStream(Looper myLooper, String url,
                               TubeOptions opt, final StreamTubeListener<Object> listener) {
        Response response = null;
        ResponseBody responseBody = null;
        Handler handler = null;

        // 这种条件说明是异步请求，需要异步返回结果
        if (myLooper != null && listener != null) {
            handler = new Handler(myLooper);
        }

        try {
            response = OkHttpConnection.okHttpExecute(url, mConfig, opt);
        } catch (TubeException e) {
            // TODO Auto-generated catch block
            if (listener != null) {
                makeFailed(handler, listener, e);
            }

            return null;
        }

        if (response == null) {
            if (listener != null) {
                makeFailed(handler, listener, new TubeException(
                        "The result is null or empty."));
            }

            return null;
        }

        responseBody = response.body();

        if (handler != null) {
            try {
                final Object object = listener.doInBackground(OkHttpConnection.getStream(response));

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        listener.onSuccess(object);
                    }
                });
            } catch (Exception e) {
                makeFailed(handler, listener, new TubeException(e,
                        TubeException.DATA_ERROR_CODE));
            } finally {
                responseBody.close();
            }
        }

        return response;
    }

    private void makeFailed(Handler handler, final TubeListener<?, ?> listener,
                            final TubeException e) {
        if (handler == null || listener == null) {
            return;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                listener.onFailed(e);
            }
        });
    }
}
