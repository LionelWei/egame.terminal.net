package cn.egame.terminal.net.utils;


/*
 * FileName:    CommomUtils.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     11/1/16 1.00 初始版本
 */


import java.util.LinkedList;

public class CommomUtils {
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";

    /**
     * 获取新的host列表
     * 将"http://202.102.39.23/"这种格式的地址转换为 “202.102.39.23”
     *
     */
    public static LinkedList<String> convertHosts(LinkedList<String> hosts) {
        LinkedList<String> newHosts = new LinkedList<>();
        if (hosts != null) {
            for (String host : hosts) {
                String temp = host;
                if (temp.startsWith(HTTP_PREFIX)) {
                    temp = host.substring(HTTP_PREFIX.length());
                } else if (temp.startsWith(HTTPS_PREFIX)) {
                    temp = host.substring(HTTPS_PREFIX.length());
                }
                if (temp.endsWith("/")) {
                    temp = temp.substring(0, temp.length() - 1);
                }
                newHosts.add(temp);
            }
        }
        return newHosts;
    }
}
