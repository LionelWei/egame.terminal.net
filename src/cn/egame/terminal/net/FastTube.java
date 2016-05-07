/*
 * FileName:	FastTube.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	快速获取网络数据类，使用单实例模式
 * History:		2013-10-18 1.00 初始版本
 */
package cn.egame.terminal.net;

import java.util.LinkedList;

import android.text.TextUtils;
import cn.egame.terminal.net.core.EgameTube;
import cn.egame.terminal.net.core.HttpConnector.EntityResult;
import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.StreamTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;
import cn.egame.terminal.net.listener.TubeListener;

/**
 * 快速获取网络数据类 <br>
 * 使用此类进行数据获取的基本步骤如下：<br>
 * 1.使用{@link #getInstance()}方法获取唯一实例;<br>
 * 2.调用{@link #init(TubeConfig)}方法进行初始化操作;<br>
 * 3.调用<br>{@link #getString(String, StringTubeListener)},<br>
 * {@link #getString(String, TubeOptions, StringTubeListener)},<br>
 * {@link #getJSON(String, JSONTubeListener)},<br>
 * {@link #getJSON(String, TubeOptions, JSONTubeListener)}， <br>
 * {@link #getStream(String, StreamTubeListener)}, <br>
 * {@link #getStream(String, TubeOptions, StreamTubeListener)}, <br>
 * {@link #connectSync(String, TubeOptions)}, <br>
 * {@link #connectSyncStream(String, TubeOptions)} <br>等方法获取数据;<br>
 * 4.在需要增加主机列表时调用{@link #addHosts(String, LinkedList)}方法增加一组主机列表;<br>
 * 5.建议2步骤在Application的初始化中调用;<br>
 * 6.在不再使用时调用{@link #release()}释放资源，由于全局唯一实例，此方法不必须
 * 
 * @author Hein
 */
public class FastTube {

    private volatile static FastTube sInstance;

    private EgameTube mTube;

    /**
     * 
     * 获取实例
     * 
     * @return
     */
    public static FastTube getInstance() {
        if (sInstance == null) {
            synchronized (FastTube.class) {
                if (sInstance == null) {
                    sInstance = new FastTube();
                }
            }
        }

        return sInstance;
    }

    private FastTube() {
        mTube = new EgameTube();
    }

    /**
     * 
     * 初始化网络引擎全局配置项
     * 
     * @param cfg
     */
    public void init(TubeConfig cfg) {
        mTube.init(cfg);
    }

    public void init() {
        init(new TubeConfig.Builder().create());
    }

    /**
     * 
     * 释放资源
     */
    public void release() {
        mTube.release();
    }

    /**
     * 
     * 添加一组主机列表
     * 
     * @param key
     * @param hosts
     * @see TubeConfig.Builder#addHostList(String, LinkedList)
     */
    public void addHosts(String key, LinkedList<String> hosts) {
        mTube.addHosts(key, hosts);
    }

    /**
     * 
     * 向公共请求头列表中增加新的头信息，如果已经包含则覆盖
     * @param key
     * @param value
     */
    public void putCommonHeader(String key, String value) {
        mTube.putCommonHeader(key, value);
    }

    /**
     * 
     * 使用默认配置获取网络数据，返回一个String数据
     * 
     * @param url
     * @param listener
     */
    public void getString(String url, StringTubeListener<?> listener) {
        getString(url, null, listener);
    }

    /**
     * 
     * 使用默认配置获取网络数据，返回一个JSONObject
     * 
     * @param url
     * @param listener
     */
    public void getJSON(String url, JSONTubeListener<?> listener) {
        getJSON(url, null, listener);
    }

    public void getStream(String url, StreamTubeListener<?> listener) {
        getStream(url, null, listener);
    }

    /**
     * 
     * 自定义连接配置获取网络数据，返回一个String数据
     * 
     * @param url
     * @param opt
     * @param listener
     */
    public void getString(String url, TubeOptions opt,
            StringTubeListener<?> listener) {
        get(url, opt, listener);
    }

    /**
     * 
     * 自定义连接配置获取网络数据，返回一个JSONObject
     * 
     * @param url
     * @param opt
     * @param listener
     */
    public void getJSON(String url, TubeOptions opt,
            JSONTubeListener<?> listener) {
        get(url, opt, listener);
    }

    /**
     * 
     * 自定义连接配置获取网络数据，返回一个InputStream流
     * @param url
     * @param opt
     * @param listener
     */
    public void getStream(String url, TubeOptions opt,
            StreamTubeListener<?> listener) {
        get(url, opt, listener);
    }

    /**
     * 
     * 直接请求地址，不处理返回结果
     * @param url
     */
    public void get(String url) {
        get(url, null, null);
    }

    /**
     * 
     * 使用post方式上传数据，entity请在opt设置
     * @param url
     * @param opt
     * @param listener
     */
    public void post(String url, TubeOptions opt, StringTubeListener<?> listener) {
        get(url, opt, listener);
    }

    /**
     * 
     * 使用post方式上传数据，entity请在opt设置
     * @param url
     * @param opt
     */
    public void post(String url, TubeOptions opt) {
        if (opt == null) {
            throw new IllegalArgumentException(
                    "The opt can not be null in post method.");
        }

        post(url, opt, null);
    }

    private void get(String url, TubeOptions opt, TubeListener<?, ?> listener) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url can not be null.");
        }

        if (listener == null) {
            listener = new StringTubeListener<String>() {

                @Override
                public String doInBackground(String water) throws Exception {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void onSuccess(String result) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onFailed(TubeException e) {
                    // TODO Auto-generated method stub

                }
            };
        }

        mTube.get(url, opt, listener);
    }

    /**
     * 
     * 同步返回String数据
     * @param url
     * @param opt
     * @return 返回的字符串
     */
    public String connectSync(String url, TubeOptions opt) {
        return mTube.connectString(url, opt);
    }

    /**
     * 
     * 同步返回流对象
     * @param url
     * @param opt
     * @return 封装后包含流的对象
     * @see cn.egame.terminal.net.core.HttpConnector.EntityResult
     */

    // 这个接口有点不合理 将关闭连接交给上层来处理 容易造成内存泄漏
    // 先去掉..
//    public  connectSyncStream(String url, TubeOptions opt) {
//        return mTube.connectStream(url, opt);
//    }
}
