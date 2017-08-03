package cn.egame.terminal.net.core.okhttp.https;


/*
 * FileName:    HttpUtils.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/19/16 1.00 初始版本
 */

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class HttpsUtils {
    public static OkHttpClient.Builder httpsBuilder(OkHttpClient.Builder builder) {
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