package cn.egame.terminal.net.core;


import org.apache.http.Header;

import java.net.Proxy;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpFactory {

    public static OkHttpClient client(String url, TubeOptions opt) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (url.startsWith("https")) {
            builder = addHttpsConfig(builder);
        }

        Proxy proxy = opt.proxy;
        if (proxy != null) {
            builder = builder.proxy(proxy);
        }

        int readTimeOut = opt.readTimeOut;
        if (readTimeOut > 0) {
            if (readTimeOut > 1000) {
                readTimeOut /= 1000;
            }
            builder = builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        }

        int connTimeOut = opt.connTimeOut;
        if (connTimeOut > 0) {
            if (connTimeOut > 1000) {
                connTimeOut /= 1000;
            }
            builder = builder.connectTimeout(connTimeOut, TimeUnit.SECONDS);
        }

        return builder.build();
    }


    public static Request request(String url, TubeOptions opt,
                                  LinkedList<String> hosts,
                                  Hashtable<String, Integer> indexMap) {
        Request.Builder builder = new Request.Builder();

        String hostKey = opt.hostKey;
        builder = builder.url(processUrls(url, hostKey, hosts, indexMap));

        // by JakeWharton: "Accept-Encoding: gzip"会被自动添加到请求中, 并且在回复时自动解压缩
        // builder = builder.addHeader("Accept-Encoding", "gzip");

        int method = opt.httpMethod;
        if (method == TubeOptions.HTTP_METHOD_POST) {
            FormBody body = opt.formBody;
            builder = builder.post(body);
        }

        // 为了兼容HttpClient, 即将弃用
        List<Header> headers = opt.listHeaders;
        if (headers != null) {
            for (Header header : headers) {
                String name = header.getName();
                String value = header.getValue();
                builder = builder.addHeader(name, value);
            }
        }

        // 设置key-values 形式的 header
        Map<String, String> keyValues = opt.mapHeaders;
        if (keyValues != null) {
            for (String key : keyValues.keySet()) {
                builder = builder.addHeader(key, keyValues.get(key));
            }
        }

        return builder.build();
    }

    /**
     * 根据需要修改请求地址的主机和端口号
     *
     */
    private static String processUrls(String oldUrl, String hostKey,
                                      LinkedList<String> hosts,
                                      Hashtable<String, Integer> indexMap) {
        // 如果没有主机切换地址或者从来没有切换过，则直接返回
        if (hosts == null || hosts.isEmpty() || !indexMap.containsKey(hostKey)) {
            return oldUrl;
        }

        URI uri = URI.create(oldUrl);
        String authority = uri.getAuthority();
        Integer index = indexMap.get(hostKey);

        // 检查index是否越界
        if (index == null || hosts.size() <= index) {
            return oldUrl;
        }
        URI newServerURI = URI.create(hosts.get(index));
        return oldUrl.replaceFirst(authority, newServerURI.getAuthority());
    }

    private static OkHttpClient.Builder addHttpsConfig(OkHttpClient.Builder builder) {
        try {
            builder.sslSocketFactory(getSslSocketFactory());
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

    private static SSLSocketFactory getSslSocketFactory() throws Exception{
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
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
                }
        };
        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        return sslContext.getSocketFactory();
    }
}
