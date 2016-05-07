package cn.egame.terminal.net.core;


import org.apache.http.Header;

import java.net.URI;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpFactory {

    public static OkHttpClient client(TubeOptions opt) {
        int soTimeOut = opt.mSoTimeOut;
        int connTimeOut = opt.mConnTimeOut;

        OkHttpClient client;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // proxy
        // TODO

        if (soTimeOut > 0) {
            if (soTimeOut > 1000) {
                soTimeOut /= 1000;
            }
            builder = builder.readTimeout(soTimeOut, TimeUnit.SECONDS);
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

        String hostKey = opt.mHostKey;
        builder = builder.url(processUrls(url, hostKey, hosts, indexMap));
        builder = builder.addHeader("Accept-Encoding", "gzip");

        // 为了兼容HttpClient, 即将弃用
        List<Header> headers = opt.mListHeaders;
        for (Header header: headers) {
            String name = header.getName();
            String value = header.getValue();
            builder = builder.addHeader(name, value);

        }

        // 设置key-values 形式的 header
        Map<String, String> keyValues = opt.mMapHeaders;
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
