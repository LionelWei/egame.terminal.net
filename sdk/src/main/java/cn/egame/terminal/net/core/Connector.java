package cn.egame.terminal.net.core;


/*
 * FileName:    Connector.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/21/16 1.00 初始版本
 */


import cn.egame.terminal.net.core.okhttp.OkHttpConnector;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.TubeListener;

public abstract class Connector {
    public static final int READ_TIMEOUT = 15 * 1000;
    public static final int CONN_TIMEOUT = 15 * 1000;
    public static final int RECONN_TIMES = 4;

    protected String mUrl;
    protected TubeConfig mConfig;
    protected TubeOptions mOptions;
    protected TubeListener<?, ?> mListener;

    // 目前只支持OkHttp
    // 需要添加HttpURLConnection的适配
    // 这样可以在没有导入OKHttp的情况下使用SDK
    public static Connector createBy(String url) {
        return createBy(url, null, null, null);
    }

    public static Connector createBy(String url,
                                           TubeConfig config,
                                           TubeOptions option) {
        return createBy(url, config, option, null);
    }

    public static Connector createBy(String url,
                                           TubeConfig config,
                                           TubeOptions option,
                                           TubeListener<?, ?> listener) {
        Connector connector = OkHttpConnector.createIfSupported();
        if (connector != null) {
            return connector
                    .url(url)
                    .config(config)
                    .option(option)
                    .listener(listener);
        } else {
            throw new IllegalArgumentException("no other connector supported");
            // 适配huc, TODO
//            return null;
        }
    }

    protected Connector() {
    }

    public Connector url(String url) {
        mUrl = url;
        return this;
    }

    public Connector config(TubeConfig config) {
        mConfig = config;
        return this;
    }

    public Connector option(TubeOptions option) {
        mOptions = option;
        return this;
    }

    public Connector listener(TubeListener<?, ?> listener) {
        mListener = listener;
        return this;
    }

    // 异步请求
    public abstract void enqueue(TubeCallback callback);

    // 同步请求
    public abstract TubeResponse execute() throws TubeException;
}