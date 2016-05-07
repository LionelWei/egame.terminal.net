package cn.egame.terminal.net.core;


import org.apache.http.Header;

import java.net.Proxy;
import java.net.URI;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttpFactory {

    public static OkHttpClient client(TubeOptions opt) {
        int readTimeOut = opt.readTimeOut;
        int connTimeOut = opt.connTimeOut;
        Proxy proxy = opt.proxy;

        OkHttpClient client;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // https 还没搞懂
        // TODO
        if (proxy != null) {
            builder = builder.proxy(proxy);
        }

        if (readTimeOut > 0) {
            if (readTimeOut > 1000) {
                readTimeOut /= 1000;
            }
            builder = builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        }

        if (connTimeOut > 0) {
            if (connTimeOut > 1000) {
                connTimeOut /= 1000;
            }
            builder = builder.connectTimeout(connTimeOut, TimeUnit.SECONDS);
        }

        client = builder.build();
        return client;
    }


    public static Request request(String url, TubeOptions opt,
                                  LinkedList<String> hosts,
                                  Hashtable<String, Integer> indexMap) {
        Request request;
        Request.Builder builder = new Request.Builder();

        int method = opt.httpMethod;
        String hostKey = opt.hostKey;
        builder = builder.url(processUrls(url, hostKey, hosts, indexMap));
        builder = builder.addHeader("Accept-Encoding", "gzip");

        if (method == TubeOptions.HTTP_METHOD_POST) {
            FormBody body = opt.formBody;
            builder = builder.post(body);
        }

        // 为了兼容HttpClient, 即将弃用
        List<Header> headers = opt.listHeaders;
        for (Header header: headers) {
            String name = header.getName();
            String value = header.getValue();
            builder = builder.addHeader(name, value);

        }

        // 设置key-values 形式的 header
        Map<String, String> keyValues = opt.mapHeaders;
        if (keyValues != null) {
            for (String key : keyValues.keySet()) {
                builder = builder.addHeader(key, keyValues.get(key));
            }
        }

        request = builder.build();
        return request;
    }


    /**
     * 根据需要修改请求地址的主机和端口号
     *
     * @param oldUrl
     * @param hosts
     * @return newurl
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
}
