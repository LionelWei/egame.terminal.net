package cn.egame.terminal.net.core;


import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import org.apache.http.Header;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttpFactory {

    // 指定cache的目录名
    private static String CACHE_FILE = "egame.cache.tmp";

    // 根据官方文档描述，为了提升效率和使用OkHttp的特性（如共享回复缓存、线程池、连接重用等）
    // 应该使用OkHttpClient单例模式
    private static OkHttpClient mOkHttpClient;
    private static boolean mIsCacheSetup = false;

    public static OkHttpClient client(String url, TubeOptions opt,
                                      Dns dns, Interceptor interceptor, String cacheDir) {
        if (mOkHttpClient == null) {
            synchronized (OkHttpFactory.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = createOkHttpClient(opt);
                }
            }
        }

        // 设置cache存储路径
        if (!TextUtils.isEmpty(cacheDir)) {
            File cacheFile = new File(cacheDir, CACHE_FILE);
            if (!cacheFile.exists()) {
                Logger.d("Cache dir: " + cacheFile.getAbsolutePath());
                synchronized (OkHttpFactory.class) {
                    if (!cacheFile.exists()) {
                        int cacheSize = 10 * 1024 * 1024; // 10 MiB
                        Cache cache = new Cache(cacheFile, cacheSize);
                        mOkHttpClient = mOkHttpClient.newBuilder().cache(cache).build();
                    }
                }
            } else {
                if (!mIsCacheSetup) {
                    synchronized (OkHttpFactory.class) {
                        if (!mIsCacheSetup) {
                            mIsCacheSetup = true;
                            int cacheSize = 10 * 1024 * 1024; // 10 MiB
                            Cache cache = new Cache(cacheFile, cacheSize);
                            mOkHttpClient = mOkHttpClient.newBuilder().cache(cache).build();
                        }
                    }
                }
            }
        }

        return newClient(url, opt, dns, interceptor);
    }

    public static Request request(String url, TubeOptions opt) {
        Request.Builder builder = new Request.Builder();

        builder = builder.url(url);

        // by JakeWharton: "Accept-Encoding: gzip"会被自动添加到请求中, 并且在回复时自动解压缩
        // builder = builder.addHeader("Accept-Encoding", "gzip");

        if (opt != null) {
            int method = opt.mHttpMethod;
            if (method == TubeOptions.HTTP_METHOD_POST) {
                RequestBody body = opt.mRequestBody;
                builder = builder.post(body);
            }

            // 为了兼容HttpClient, 即将弃用
            List<Header> headers = opt.mListHeaders;
            if (headers != null) {
                for (Header header : headers) {
                    String name = header.getName();
                    String value = header.getValue();
                    builder = builder.addHeader(name, value);
                }
            }

            // 设置key-values 形式的 header
            Map<String, String> keyValues = opt.mMapHeaders;
            if (keyValues != null) {
                for (String key : keyValues.keySet()) {
                    builder = builder.addHeader(key, keyValues.get(key));
                }
            }
        }

        return builder.build();
    }

    private static OkHttpClient createOkHttpClient(TubeOptions opt) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (opt != null) {
            // 无需设置超时，使用okhttp的默认配置
/*
            int readTimeOut = opt.mReadTimeOut;
            if (readTimeOut > 0) {
                if (readTimeOut > 1000) {
                    readTimeOut /= 1000;
                }
                builder = builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
            }

            int connTimeOut = opt.mConnTimeOut;
            if (connTimeOut > 0) {
                if (connTimeOut > 1000) {
                    connTimeOut /= 1000;
                }
                builder = builder.connectTimeout(connTimeOut, TimeUnit.SECONDS);
            }
*/
        }
        return builder.build();
    }

    private static OkHttpClient newClient(String url, TubeOptions opt,
                                          Dns dns, Interceptor interceptor) {
        // 复用okhttpclient的配置
        OkHttpClient.Builder builder = mOkHttpClient.newBuilder();

        if (opt != null) {
            // 代理
            if (opt.mProxy != null) {
                builder.proxy(opt.mProxy);
            }

            // dns
            if (dns != null) {
                builder.dns(dns);
            }

            // 拦截器
            if (interceptor != null) {
                builder.addInterceptor(interceptor);
            }

            // https配置
            if (url.startsWith("https")) {
                builder = httpsBuilder(builder);
            }
        }

        return builder.build();
    }

    private static OkHttpClient.Builder httpsBuilder(OkHttpClient.Builder builder) {
        try {
            builder.sslSocketFactory(getSslSocketFactory(), mTrustAllCerts);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static X509TrustManager mTrustAllCerts = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };

    private static SSLSocketFactory getSslSocketFactory() throws Exception{
        // 为了避免需要证书，忽略校验过程
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { mTrustAllCerts },
                new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }
}
