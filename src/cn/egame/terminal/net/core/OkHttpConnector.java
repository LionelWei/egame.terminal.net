package cn.egame.terminal.net.core;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

import cn.egame.terminal.net.exception.TubeException;
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


    private static void waitToReconnect() {
        try {
            Thread.sleep(RECONN_INTERVAL);
        } catch (InterruptedException e) {

        }
    }

}
