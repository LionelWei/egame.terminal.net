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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.exception.TubeIOException;
import cn.egame.terminal.net.exception.TubeParseException;
import cn.egame.terminal.net.listener.TubeListener;
import cn.egame.terminal.net.parser.Parser;
import cn.egame.terminal.net.utils.CommomUtils;

/**
 * 网络管道类 对于配置信息相同仅URL不同的请求可共用一个对象
 *
 * @author Hein
 */
public class EgameTube {

    private static final String TAG = "TUBE";

    private TubeConfig mConfig = TubeConfig.getDefault();

    public EgameTube() {
    }

    public void init(TubeConfig cfg) {
        mConfig = cfg;
    }

    public void addHosts(String key, LinkedList<String> hosts) {
        if (mConfig != null) {
            mConfig.putHost(key, CommomUtils.convertHosts(hosts));
        }
    }

    public void putCommonHeader(String key, String value) {
        if (mConfig != null) {
            mConfig.putCommonHeader(key, value);
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
        if (listener == null) {
            throw new IllegalArgumentException("The listener can not be null.");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
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
            TubeResponse response = Connector.createBy(url, mConfig, opt).execute();
            result = response.getString();
        } catch (TubeException e) {
            e.printStackTrace();
        }
        return result;
    }

    public EntityResult connectStream(final String url, final TubeOptions opt) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        TubeResponse response = null;
        try {
            response = Connector.createBy(url, mConfig, opt).execute();
        } catch (TubeException e) {
            e.printStackTrace();
        }
        return new EntityResult(response);
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

        Connector.createBy(url, mConfig, opt, listener)
                .enqueue(new TubeCallback() {
                    @Override
                    public void onFailure(TubeException e) {
                        makeFailed(handler, listener, new TubeIOException(e));
                    }

                    @Override
                    public void onResponse(TubeResponse response) throws TubeException {
                        handleResponse(response, handler, listener);
                    }
                });
    }

    private <P, T> void handleResponse(TubeResponse response,
                                       Handler handler,
                                       final TubeListener<P, T> listener) {
        try {
            Parser<P, T> parser = Parser.create(response, listener);
            T result = parser.parseResult();
            final T object = (result == null)
                    ? listener.doInBackground(parser.parseParam())
                    : result;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(object);
                }
            });
        } catch (Exception e) {
            makeFailed(handler, listener, new TubeParseException(e));
        } finally {
            // 如果result返回的是stream, 需要手动关闭
            response.close();
        }
    }

    private void makeFailed(Handler handler, final TubeListener<?, ?> listener,
                            final TubeException e) {
        if (handler == null || listener == null) {
            return;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                listener.onFailed(e);
            }
        });
    }

}
