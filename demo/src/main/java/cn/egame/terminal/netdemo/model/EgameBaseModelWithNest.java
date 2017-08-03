package cn.egame.terminal.netdemo.model;


/*
 * FileName:    EgameBaseModelWithNest.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     3/21/17 1.00 初始版本
 */


import java.util.List;

public class EgameBaseModelWithNest {
    public int code;
    public String text;
    public ExtBean ext;

    public static class ExtBean {
        /**
         * host_url : ["http://202.102.39.23/","http://180.96.49.15/","http://180.96.49.16/"]
         * app_key :
         * cdn_url : []
         */

        public String app_key;
        public List<String> host_url;
        public List<?> cdn_url;

        @Override
        public String toString() {
            return "ExtBean{" +
                    "app_key='" + app_key + '\'' +
                    ", host_url=" + host_url +
                    ", cdn_url=" + cdn_url +
                    '}';
        }
    }

}
