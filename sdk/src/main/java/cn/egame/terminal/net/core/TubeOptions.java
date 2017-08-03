/*
 * FileName:	TubeOptions.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	每个请求所使用的连接选项
 * History:		2013-10-24 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import android.text.TextUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import cn.egame.terminal.net.data.MultipartFormData;

/**
 * 每个请求所使用的连接选项
 *
 * @author Hein
 * @see Builder
 */
public class TubeOptions {

    /**
     * 使用HTTP GET 发起请求
     */
    public static final int HTTP_METHOD_GET = 0;
    public static final int HTTP_METHOD_POST = 1;

    private int mReadTimeOut = -1;
    private int mConnTimeOut = -1;
    private int mReConnTimes = -1;
    private Map<String, String> mMapHeaders = null;
    private String mHostKey = null;
    private int mHttpMethod = -1;
    private Proxy mProxy = null;
    private MultipartFormData mFormData;

    private TubeOptions() {

    }

    public int getReadTimeOut() {
        return mReadTimeOut;
    }

    public int getConnectTimeOut() {
        return mConnTimeOut;
    }

    public int getReconnectTimeOut() {
        return mReConnTimes;
    }

    public void setHeader(Map<String, String> headers) {
        if (mMapHeaders == null) {
            mMapHeaders = new HashMap<>();
        }
        mMapHeaders.putAll(headers);
    }

    public Map<String, String> getHeaders() {
        return mMapHeaders;
    }

    public String getHostKey() {
        return mHostKey;
    }

    public int getMethod() {
        return mHttpMethod;
    }

    public Proxy getProxy() {
        return mProxy;
    }

    public MultipartFormData getMultiPartFormData() {
        return mFormData;
    }


    /**
     *
     * EgameTube建造器
     *
     * @author Hein
     */
    public static class Builder {

        private int mReadTimeOut = Connector.READ_TIMEOUT;
        private int mConnTimeOut = Connector.CONN_TIMEOUT;
        private int mReconnTimes = Connector.RECONN_TIMES;
        private Map<String, String> mMapHeaders = null;
        private String mHostKey = null;
        private int mHttpMethod = HTTP_METHOD_GET;
        // OkHttp相关配置
        private Proxy mProxy;
        private MultipartFormData mFormData;

        public Builder() {

        }


        /**
         *
         * 设置socket超时时间
         *
         * @param time
         * @return
         */
        public Builder setSoTimeOut(int time) {
            this.mReadTimeOut = time;
            return this;
        }

        /**
         *
         * 设置连接超时时间
         *
         * @param time
         * @return
         */
        public Builder setConnectionTimeOut(int time) {
            this.mConnTimeOut = time;
            return this;
        }

        /**
         *
         * 设置请求头列表
         *
         * @param headers
         * @return
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.mMapHeaders = headers;
            return this;
        }

        // post key-value
        // TODO


        public Builder setRequestBody(MultipartFormData formData) {
            mFormData = formData;
            return this;
        }

        /**
         *
         * 设置代理地址
         *
         * @param hostname 主机ip
         * @param port 端口号
         * @return
         */
        public Builder setProxy(String hostname, int port) {
            if (TextUtils.isEmpty(hostname) || port < 0) {
                return this;
            }
            mProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
            return this;
        }

        /**
         *
         * 设置重连尝试次数
         * @param times
         * @return
         */
        public Builder setReconnectionTimes(int times) {
            this.mReconnTimes = times;
            return this;
        }

        /**
         *
         * 设置重连时切换主机所使用的主机列表的Key
         * @param key
         * @return
         */
        public Builder setHostKey(String key) {
            if (TextUtils.isEmpty(key)) {
                key = "";
            }

            this.mHostKey = key;
            return this;
        }

        /**
         *
         * 设置 HTTP请求方式 ,如果不设置,默认为Get请求,如果设置了postEntity,则默认为Post请求
         * @param method
         * @return
         * @see TubeOptions#HTTP_METHOD_GET HTTP_METHOD_GET
         * @see TubeOptions#HTTP_METHOD_POST HTTP_METHOD_POST
         */
        public Builder setHttpMethod(int method) {
            if (method == HTTP_METHOD_POST) {
                this.mHttpMethod = HTTP_METHOD_POST;
            }

            return this;
        }

        /**
         *
         * 创建并返回EgameTube对象
         *
         * @return
         */
        public TubeOptions create() {
            TubeOptions option = new TubeOptions();

            option.mConnTimeOut = this.mConnTimeOut;
            option.mReadTimeOut = this.mReadTimeOut;
            option.mReConnTimes = this.mReconnTimes;
            option.mMapHeaders = this.mMapHeaders;
            option.mHostKey = this.mHostKey;
            option.mProxy = this.mProxy;
            option.mFormData = this.mFormData;

            if (this.mFormData != null) {
                option.mHttpMethod = HTTP_METHOD_POST;
                // post太耗费流量，如果一次post失败建议不再重试
                // option.mReConnTimes = 0;
            } else {
                option.mHttpMethod = HTTP_METHOD_GET;
            }

            return option;
        }

    }
}
