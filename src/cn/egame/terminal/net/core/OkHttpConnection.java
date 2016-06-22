package cn.egame.terminal.net.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpConnection {

    public static final int READ_TIMEOUT = 15 * 1000;
    public static final int CONN_TIMEOUT = 15 * 1000;
    public static final int RECONN_TIMES = 4;
    private static final int RECONN_INTERVAL = 2 * 1000;

    private String mUrl;
    private TubeConfig mConfig;
    private TubeOptions mOptions;
    private Dns mDns;
    private Interceptor mInterceptor;
    private String mCacheDir;

    public void enqueue(Callback responseCallback) {
        OkHttpClient okHttpClient = OkHttpFactory.client(mUrl, mOptions,
                                                        mDns, mInterceptor, mCacheDir);
        Request request = OkHttpFactory.request(mUrl, mOptions);
        okHttpClient.newCall(request).enqueue(responseCallback);
    }

    public Response execute() throws IOException{
        OkHttpClient okHttpClient = OkHttpFactory.client(mUrl, mOptions,
                                                        mDns,mInterceptor, mCacheDir);
        Request request = OkHttpFactory.request(mUrl, mOptions);
        return okHttpClient.newCall(request).execute();
    }

    public static String getString(Response response) {
        try {
            if (response != null && response.body() != null) {
                // by JakeWharton: "Accept-Encoding: gzip"会被自动添加到请求中, 并且在回复时自动解压缩
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static InputStream getStream(Response response) {
        if (response != null && response.body() != null) {
            return response.body().byteStream();
        }
        return null;
    }

    private OkHttpConnection(OkHttpConnection.Builder builder) {
        mUrl = builder.mUrl;
        mConfig = builder.mConfig;
        mOptions = builder.mOptions;
        mInterceptor = builder.mInterceptor;
        if (mConfig != null) {
            mCacheDir = mConfig.mFileDir;

            if (mOptions == null) {
                mOptions = mConfig.mDefaultOptions;
            }
            if (mOptions.mMapHeaders == null) {
                mOptions.mMapHeaders = mConfig.mCommonHeaders;
            } else {
                mOptions.mMapHeaders.putAll(mConfig.mCommonHeaders);
            }
            LinkedList<String> hostList = mConfig.mHosts.get(mOptions.mHostKey);
            mDns = new OkHttpDns.Builder().url(mUrl).addresses(hostList).build();
        }
    }

    public static final class Builder {
        protected String mUrl;
        protected TubeConfig mConfig;
        protected TubeOptions mOptions;
        protected Interceptor mInterceptor;

        public Builder url(String url) {
            this.mUrl = url;
            return this;
        }

        public Builder config(TubeConfig tubeConfig) {
            this.mConfig = tubeConfig;
            return this;
        }

        public Builder option(TubeOptions options) {
            this.mOptions = options;
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            this.mInterceptor = interceptor;
            return this;
        }

        public OkHttpConnection build() {
            return new OkHttpConnection(this);
        }
    }
}
