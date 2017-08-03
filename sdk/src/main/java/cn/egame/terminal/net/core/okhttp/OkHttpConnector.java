package cn.egame.terminal.net.core.okhttp;

import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.egame.terminal.net.core.Connector;
import cn.egame.terminal.net.core.TubeCallback;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.core.okhttp.https.HttpsUtils;
import cn.egame.terminal.net.core.okhttp.interceptors.HttpLoggingInterceptor;
import cn.egame.terminal.net.core.okhttp.interceptors.PreparationInterceptor;
import cn.egame.terminal.net.data.MultipartFormData;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.exception.TubeIOException;
import cn.egame.terminal.utils.ELog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpConnector extends Connector {
    private static final String TAG = "OkHttp";

    private Dns mDns;
    private Interceptor mInterceptor;
    private String mCacheDir;


    public static Connector createIfSupported() {
        try {
            Class.forName("okhttp3.OkHttpClient");
            return new OkHttpConnector();
        } catch (ClassNotFoundException e) {
            Log.e("EgameNet", "OkHttp doesn't exist, switch to Hurl");
        }
        return null;
    }

    private OkHttpConnector() {
        init();
    }

    @Override
    public void enqueue(final TubeCallback callback) {
        getClient().newCall(getRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(new TubeIOException(e.getLocalizedMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    callback.onResponse(new OkHttpResponse(response));
                } catch (TubeException e) {
                    throw new TubeIOException("");
                }
            }
        });
    }

    @Override
    public TubeResponse execute() throws TubeException{
        try {
            Response response = getClient().newCall(getRequest()).execute();
            return new OkHttpResponse(response);
        } catch (IOException e) {
            throw new TubeIOException(e.getLocalizedMessage());
        }
    }

    private void init() {
        mInterceptor = new PreparationInterceptor(mListener);
        if (mConfig != null) {
            mCacheDir = mConfig.getFileDir();

            if (mOptions == null) {
                mOptions = mConfig.getDefaultOptions();
            }
            mOptions.setHeader(mConfig.getCommonHeaders());
            LinkedList<String> hostList = mConfig.getHosts().get(mOptions.getHostKey());
            mDns = new OkHttpDns(mUrl, hostList);
        }
    }

    private OkHttpClient getClient() {
        OkHttpClient defaultClient = getDefault();
        // 复用OkHttpClient的配置
        OkHttpClient.Builder builder = defaultClient.newBuilder();

        if (mOptions != null) {
            // 超时
            int readTimeOut = mOptions.getReadTimeOut();
            if (readTimeOut > 1000) {
                builder = builder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
            }

            int connTimeOut = mOptions.getConnectTimeOut();
            if (connTimeOut > 1000) {
                builder = builder.connectTimeout(connTimeOut, TimeUnit.MILLISECONDS);
            }
            // 代理
            if (mOptions.getProxy() != null) {
                builder.proxy(mOptions.getProxy());
            }

            // dns
            if (mDns != null) {
                builder.dns(mDns);
            }

            // 拦截器
            if (mInterceptor != null) {
                builder.addInterceptor(mInterceptor);
            }

            // https配置
            if (mUrl.startsWith("https")) {
                builder = HttpsUtils.httpsBuilder(builder);
            }
        }

        if (ELog.LOG_ON) {
            HttpLoggingInterceptor.Logger logger = new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    ELog.v("OkHttp", message);
                }
            };
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(logger);
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder = builder.addNetworkInterceptor(logging);
        }

        return builder.build();

    }

    private Request getRequest() {
        Request.Builder builder = new Request.Builder();

        builder = builder.url(mUrl);

        // by JakeWharton: "Accept-Encoding: gzip"会被自动添加到请求中, 并且在回复时自动解压缩
        // builder = builder.addHeader("Accept-Encoding", "gzip");

        if (mOptions != null) {
            int method = mOptions.getMethod();
            if (method == TubeOptions.HTTP_METHOD_POST) {
                MultipartFormData formData = mOptions.getMultiPartFormData();
                RequestBody body = OkHttpMultipart.getRequestBody(formData);
                builder = builder.post(body);
            }

            // 设置key-values 形式的 header
            Map<String, String> keyValues = mOptions.getHeaders();
            if (keyValues != null) {
                for (String key : keyValues.keySet()) {
                    builder = builder.addHeader(key, keyValues.get(key));
                }
            }
        }

        return builder.build();
    }

    private static OkHttpClient getDefault() {
        return LazyLoad.instance;
    }

    private static class LazyLoad {
        static OkHttpClient instance = new OkHttpClient();
    }

}
