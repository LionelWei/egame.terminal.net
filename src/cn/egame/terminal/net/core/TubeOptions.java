/*
 * FileName:	TubeOptions.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	每个请求所使用的连接选项
 * History:		2013-10-24 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import android.content.ContentValues;
import android.text.TextUtils;

import okhttp3.FormBody;

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

    /**
     * 使用HTTP POST 发起请求
     */
    public static final int HTTP_METHOD_POST = 1;

    protected int mSoTimeOut = -1;

    protected int mConnTimeOut = -1;

    protected int mReconnTimes = -1;

    protected Map<String, String> mMapHeaders = null;

    protected List<Header> mListHeaders = null;

    protected HttpEntity mPostEntity = null;
    
    protected boolean isPostInGzip = false;

    protected HttpHost mHttpProxy = null;

    protected String mHostKey = null;

    protected int mHttpMethod = -1;

    private TubeOptions() {

    }

    /**
     * 
     * EgameTube建造器
     * 
     * @author Hein
     */
    public static class Builder {

        private int mSoTimeOut = HttpConnector.SO_TIMEOUT;

        private int mConnTimeOut = HttpConnector.CONN_TIMEOUT;

        private int mReconnTimes = HttpConnector.RECONN_TIMES;

        private Map<String, String> mMapHeaders = null;

        private List<Header> mListHeaders = null;

        private HttpEntity mPostEntity = null;

        private boolean isPostInGzip = false;

        private HttpHost mHttpProxy = null;

        private String mHostKey = null;

        private int mHttpMethod = HTTP_METHOD_GET;

        private FormBody mFormBody;

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
            this.mSoTimeOut = time;
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

        /**
         * 
         * 设置请求头列表
         * 
         * @param headers
         * @return
         */
        @Deprecated
        public Builder setHeaders(List<Header> headers) {
            this.mListHeaders = headers;
            return this;
        }

        /**
         * 设置post使用的Entity, NameValuePair形式
         * @param parameters NameValuePair列表 参见{@link org.apache.http.message.BasicNameValuePair}
         * @return
         */
        public Builder setPostEntity(List<? extends NameValuePair> parameters) {
            ContentValues values = new ContentValues();
            return setPostEntity(parameters, "utf-8");
        }


        /**
         * 设置post使用的Entity, NameValuePair形式
         * @param parameters NameValuePair列表 参见{@link org.apache.http.message.BasicNameValuePair}
         * @param charset 指定字符集
         * @return
         */
        public Builder setPostEntity(List<? extends NameValuePair> parameters,
                String charset) {
            try {
                this.mPostEntity = new UrlEncodedFormEntity(parameters, charset);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                throw new IllegalArgumentException(e.getLocalizedMessage());
            }
            return this;
        }

        /**
         * 设置post使用的Entity, HttpEntity形式
         * @param entity 支持各种实现体 <Br/> 一般地: <Br/>文件{@link org.apache.http.entity.FileEntity} <Br/>
         * 文本{@link org.apache.http.entity.StringEntity} <Br/>
         * 输入流{@link org.apache.http.entity.InputStreamEntity}
         * @return
         */
        public Builder setPostEntity(HttpEntity entity) {
            this.mPostEntity = entity;

            return this;
        }

        public Builder setPostBody(ContentValues cv) {
            Set<Map.Entry<String, Object>> entrySet = cv.valueSet();
            FormBody.Builder builder = new FormBody.Builder();
            for (Map.Entry<String, Object> entry : entrySet) {
                builder = builder.add(entry.getKey(), (String)entry.getValue());
                // addEncoded?
            }
            mFormBody = builder.build();
        }

        public Builder enablePostInGzip() {
            this.isPostInGzip = true;
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
            // OKHttp用的是Java的proxy 还需要研究下
            this.mHttpProxy = new HttpHost(hostname, port);
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
            option.mSoTimeOut = this.mSoTimeOut;
            option.mReconnTimes = this.mReconnTimes;
            option.mMapHeaders = this.mMapHeaders;
            option.mListHeaders = this.mListHeaders;
            option.mHttpProxy = this.mHttpProxy;
            option.mHostKey = this.mHostKey;
            option.mPostEntity = this.mPostEntity;
            option.isPostInGzip = this.isPostInGzip;

            if (this.mPostEntity != null) {
                option.mHttpMethod = HTTP_METHOD_POST;
            } else {
                option.mHttpMethod = this.mHttpMethod;
            }

            return option;
        }

    }
}
