/*
 * FileName:	EgameBox.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Set;

import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.StreamTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;
import cn.egame.terminal.net.listener.TubeListener;
import cn.egame.terminal.utils.ELog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 网络管道类 对于配置信息相同仅URL不同的请求可共用一个对象
 *
 * @author Hein
 */
public class EgameTube {

    private static final String TAG = "TUBE";
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";

    private TubeConfig mConfig = TubeConfig.getDefault();

    public EgameTube() {
    }

    public void init(TubeConfig cfg) {
        mConfig = cfg;
    }

    public void addHosts(String key, LinkedList<String> hosts) {
        if (mConfig != null) {
            mConfig.mHosts.put(key, convertHosts(hosts));
        }
    }

    public void putCommonHeader(String key, String value) {
        if (mConfig != null) {
            mConfig.mCommonHeaders.put(key, value);
        }
    }

    public void release() {
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
        get(null, url, opt, listener);
    }

    public void get(Context context, final String url, final TubeOptions opt,
                    final TubeListener<?, ?> listener) {
        mConfig.setFileDir(context);

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

        enqueueWithListener(url, opt, listener);
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

        String result = null;
        try {
            Response response = new OkHttpConnection.Builder()
                    .url(url)
                    .config(mConfig)
                    .option(opt)
                    .build()
                    .execute();
            result = OkHttpConnection.getString(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public EntityResult connectStream(final String url, final TubeOptions opt) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        Response response = null;
        try {
            response = new OkHttpConnection.Builder()
                                    .url(url)
                                    .config(mConfig)
                                    .option(opt)
                                    .build()
                                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EntityResult(response);
    }

    /**
     * 获取新的host列表
     * 将"http://202.102.39.23/"这种格式的地址转换为 “202.102.39.23”
     *
     */
    private LinkedList<String> convertHosts(LinkedList<String> hosts) {
        LinkedList<String> newHosts = new LinkedList<String>();
        if (hosts != null) {
            for (String host : hosts) {
                String temp = host;
                if (temp.startsWith(HTTP_PREFIX)) {
                    temp = host.substring(HTTP_PREFIX.length());
                } else if (temp.startsWith(HTTPS_PREFIX)) {
                    temp = host.substring(HTTPS_PREFIX.length());
                }
                if (temp.endsWith("/")) {
                    temp = temp.substring(0, temp.length() - 1);
                }
                newHosts.add(temp);
            }
        }
        return newHosts;
    }

    private <P, T> void enqueueWithListener(final String url,
                                            final TubeOptions opt,
                                            final TubeListener<P, T> listener) {
        Looper myLooper = Looper.myLooper();
        // 如果本线程没有looper则使用主线程looper 有风险 wei.han 20131031
        if (myLooper == null) {
            myLooper = Looper.getMainLooper();
        }

        final Handler handler = new Handler(myLooper);

        new OkHttpConnection.Builder()
                .url(url)
                .config(mConfig)
                .option(opt)
                .interceptor(new PreparationInterceptor(listener))
                .build()
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        makeFailed(handler, listener,
                                new TubeException(e.getLocalizedMessage(),
                                        TubeException.DATA_ERROR_CODE));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        handleResponse(response, url, opt, handler, listener);
                    }
                });
    }

    private <P, T> void handleResponse(Response response, String url, TubeOptions opt,
                                       Handler handler, final TubeListener<P, T> listener) {
        P result = null;
        ResponseBody responseBody = response.body();
        try {
            result = getResult(response, listener);
            ELog.d("CACHE", "cache response: " + response.cacheResponse());
//            ELog.d("CACHE", "network response: " + response.networkResponse());
            final T object = listener.doInBackground(result);
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
            // 如果result返回的是stream, 需要手动关闭
            if (responseBody != null) {
                responseBody.close();
            }
        }

        if (ELog.LOG_ON && !(listener instanceof StreamTubeListener)) {
            String headerStr = "";
            if (opt != null && opt.mMapHeaders != null && !opt.mMapHeaders.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                Set<String> headers = opt.mMapHeaders.keySet();
                for (String key : headers) {
                    sb.append(key + ":" + opt.mMapHeaders.get(key) + ";");
                }
                headerStr = sb.toString();
            }

            StringBuilder log = new StringBuilder();
            log.append("Request: ");
            log.append(url);
            log.append("\n");

            log.append("----Headers: ");
            log.append(headerStr);
            log.append("\n");

            log.append("----Response: ");
            log.append("\n");

            // 对于String和Json分别做处理 (这段代码耦合性很高...)
            String newResult = null;
            if (listener instanceof StringTubeListener) {
                newResult = (String)result;
            } else if (listener instanceof JSONTubeListener) {
                newResult = result.toString();
                newResult = ELog.getJSONLogString(newResult);
            }
            log.append(newResult);

            log.append("\n");
            ELog.v(TAG, log.toString());
        }
    }

    private <P, T> P getResult(Response response, TubeListener<P, T> listener)
            throws Exception{
        P result = null;
        if (listener instanceof StringTubeListener) {
            result = (P)OkHttpConnection.getString(response);
        } else if (listener instanceof JSONTubeListener) {
            String resultStr = OkHttpConnection.getString(response);
            result = (P)new JSONObject(resultStr);
        } else if (listener instanceof StreamTubeListener) {
            result = (P)OkHttpConnection.getStream(response);
        }
        return result;
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
