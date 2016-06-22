package cn.egame.terminal.net.core;

/*
 * FileName:	OkHttpDns.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		weilai
 * Description:	手动设置dns解析地址，防止dns劫持
 * History:		2016/5/25 1.00 初始版本
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

public class OkHttpDns implements Dns {

    private List<String> mAddresses;
    private String mUrl;

    private OkHttpDns(Builder builder) {
        mAddresses = builder.mAddresses;
        mUrl = builder.mUrl;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> inetAddresses = new ArrayList<InetAddress>();

        // 如果是预先配置的host请求,则使用自定义的host列表; 否则使用系统dns解析
        // 这是为了防止手机设置代理而导致的问题
        if (mUrl != null && mUrl.contains(hostname)
                && mAddresses != null && !mAddresses.isEmpty()) {
            for (String address : mAddresses) {
                InetAddress inetAddress = InetAddress.getByName(address);
                inetAddresses.add(inetAddress);
            }
            return inetAddresses;
        }

        inetAddresses = Dns.SYSTEM.lookup(hostname);
        return inetAddresses;
    }

    public static class Builder {
        protected List<String> mAddresses;
        protected String mUrl;

        public Builder addresses(List<String> addresses) {
            mAddresses = addresses;
            return this;
        }

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public OkHttpDns build() {
            return new OkHttpDns(this);
        }
    }
}
