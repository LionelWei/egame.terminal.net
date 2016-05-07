package cn.egame.terminal.net.core;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.utils.ByteArrayBuilder;
import cn.egame.terminal.net.utils.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpConnector {

    public static final int SO_TIMEOUT = 15 * 1000;

    public static final int CONN_TIMEOUT = 15 * 1000;

    public static final int RECONN_TIMES = 4;

    private static final int RECONN_INTERVAL = 2 * 1000;

    private static final String TAG = "TUBE";

    private static Hashtable<String, Integer> sIndexMap = new Hashtable<String, Integer>();

    private static Response okHttpExecute(String url, TubeOptions opt,
                                          LinkedList<String> hosts) throws TubeException {
        OkHttpClient okHttpClient = OkHttpFactory.client(opt);
        Request request = OkHttpFactory.request(url, opt, hosts, sIndexMap);
        return getResponse(okHttpClient, request, opt, hosts);
    }

    private static Request requestWithNewHost(Request req, String hostKey,
                                              LinkedList<String> hosts) {
        if (!sIndexMap.containsKey(hostKey)) {
            sIndexMap.put(hostKey, 0);
        }
        int index = sIndexMap.get(hostKey) + 1;
        if (index >= hosts.size()) {
            index = 0;
        }
        sIndexMap.put(hostKey, index);
        String url = hosts.get(index);
        return requestWithNewUrl(req, url);
    }

    private static Request requestWithNewUrl(Request req, String newUrl) {
        return req.newBuilder().url(newUrl).build();
    }

    private static Response getResponse(OkHttpClient httpClient, Request request,
                                        TubeOptions opt, LinkedList<String> hosts) throws TubeException {
        Response response = null;
        int reConnTimes = opt.reConnTimes;
        String hostKey = opt.hostKey;

        for (int i = 0; i < reConnTimes; i++) {

            // 如果不是第一次连接，并且hosts地址不为null，则需要切换服务器地址
            if (hosts != null && !hosts.isEmpty() && i > 0) {
                request = requestWithNewHost(request, hostKey, hosts);
            }

            try {
                response = doGetResponse(httpClient, request);
                // 获取http状态码
                int statusCode = response.code();
                switch (statusCode) {
                    case 200:
                        return response;
                    case 301:
                    case 302:
                        String location = response.headers("location").get(0);
                        if (location != null) {
                            String redirectUrl = location.replaceAll(" ", "").
                                    replaceAll(String.valueOf('\t'), "");
                            request = requestWithNewUrl(request, redirectUrl);
                            continue;
                        } else {
                            throw new TubeException(
                                    "We got 302 redirect code, but no location.",
                                    TubeException.SERVER_ERROR_CODE);
                        }

                    default:
                        // http状态不正确,主动抛出异常
                        throw new TubeException("HttpStatus is not OK. -> " + statusCode,
                                TubeException.SERVER_ERROR_CODE);
                }

            } catch (IOException e) {
                // 若连接超时，等待一段时间后再次发起请求
                waitToReconnect();
            } catch (Exception e) {
                throw new TubeException("The exception is unknow: "
                        + e.getMessage(), TubeException.IO_ERROR_CODE);
            }
        }
        return response;
    }

    private static  Response doGetResponse(OkHttpClient httpClient, Request request) throws IOException {
        return httpClient.newCall(request).execute();
    }

    public static Response okHttpExecute(String url, TubeConfig cfg,
                                       TubeOptions opt) throws TubeException {
        if (cfg == null) {
            throw new TubeException("The cfg is null.");
        }

        if (opt == null) {
            opt = cfg.mDefaultOptions;
        }

        if (opt.mapHeaders == null) {
            opt.mapHeaders = cfg.mCommonHeaders;
        } else {
            opt.mapHeaders.putAll(cfg.mCommonHeaders);
        }

        return okHttpExecute(url, opt, cfg.mHosts.get(opt.hostKey));
    }



    /**
     * 兼容Chunked模式
     *
     * @param entity
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    private static String entity2String(HttpEntity entity)
            throws IllegalStateException, IOException {
        if (entity == null) {
            throw new IOException("Entity is null!");
        }
        Header contentEncoding = entity.getContentEncoding();
        boolean acceptGzip = contentEncoding != null
                && contentEncoding.getValue().equals("gzip");

        if (!entity.isChunked() && !acceptGzip) {
            return EntityUtils.toString(entity);
        }

        ByteArrayBuilder dataBuilder = new ByteArrayBuilder();

        InputStream is = entity.getContent();

        InputStream nis = null;
        byte[] buf = null;
        int count = 0;

        nis = acceptGzip ? new GZIPInputStream(is) : is;

        /*
         * accumulate enough data to make it worth pushing it up the stack
         */
        buf = new byte[8 * 1024];
        int len = 0;
        int lowWater = buf.length / 2;

        while (len != -1) {

            len = nis.read(buf, count, buf.length - count);

            if (len != -1) {
                count += len;
            }
            if (len == -1 || count >= lowWater) {
                dataBuilder.append(buf, 0, count);
                // Log.i("wei.han", "The length is " + count + " this time!");
                count = 0;
            }
        }

        if (is != null) {
            is.close();
        }

        if (nis != null) {
            nis.close();
        }

        synchronized (dataBuilder) {
            byte[] data = new byte[dataBuilder.getByteSize()];
            int offset = 0;
            while (true) {
                ByteArrayBuilder.Chunk c = dataBuilder.getFirstChunk();
                if (c == null)
                    break;

                if (c.mLength != 0) {
                    System.arraycopy(c.mArray, 0, data, offset, c.mLength);
                    offset += c.mLength;
                }
                c.release();
            }

            return new String(data);
        }
    }


    private static void waitToReconnect() {
        try {
            Thread.sleep(RECONN_INTERVAL);
        } catch (InterruptedException e) {

        }
    }


    /**
     * 创建一个自定义的额HttpCLient,包括对Https的证书忽略
     *
     * @return
     */
    private static HttpClient createHttpsClient() {

        SSLSocketFactoryEx sf = null;

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);
            sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.d(TAG, e.getLocalizedMessage());
        }

        if (sf == null) {
            return new DefaultHttpClient();
        }

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params,
                HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        schReg.register(new Scheme("https", sf, 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                params, schReg);
        return new DefaultHttpClient(conMgr, params);
    }

}
