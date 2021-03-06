/*
 * FileName:	TubeConfig.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-24 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 环境变量，公共配置项
 *
 * @author Hein
 * @see Builder
 */
public class TubeConfig {

    protected boolean isDebug = false;

    protected Map<String, LinkedList<String>> mHosts = new HashMap<>();

    protected TubeOptions mDefaultOptions;

    protected HashMap<String, String> mCommonHeaders = new HashMap<>();

    protected String mFileDir;

    private TubeConfig() {
    }

    public static TubeConfig getDefault() {
        return new TubeConfig.Builder().create();
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public Map<String, LinkedList<String>> getHosts() {
        return mHosts;
    }

    public void setHosts(Map<String, LinkedList<String>> hosts) {
        this.mHosts = hosts;
    }

    public void putHost(String key, LinkedList<String> list) {
        mHosts.put(key, list);
    }

    public TubeOptions getDefaultOptions() {
        return mDefaultOptions;
    }

    public HashMap<String, String> getCommonHeaders() {
        return mCommonHeaders;
    }

    public void setCommonHeaders(HashMap<String, String> commonHeaders) {
        mCommonHeaders.putAll(commonHeaders);;
    }

    public void putCommonHeader(String key, String value) {
        mCommonHeaders.put(key, value);
    }


    public void setFileDir(Context context) {
        if (context != null && TextUtils.isEmpty(mFileDir)) {
            mFileDir = context.getCacheDir().getAbsolutePath();
        }
    }

    public String getFileDir() {
        return mFileDir;
    }

    /**
     *
     * TubeConfig 的建造器
     *
     * @author Hein
     */
    public static class Builder {

        private boolean mIsDebug = false;

        private Map<String, LinkedList<String>> mHosts = null;

        private HashMap<String, String> mHeaders = new HashMap<>();

        private TubeOptions mDefaultOptions = null;

        public Builder() {
            mHosts = new HashMap<>();
        }

        /**
         *
         * 添加可切换的主机列表
         *
         * @param key
         *            一组主机列表的key
         * @param hosts
         *            主机列表
         * @return
         */
        public Builder addHostList(String key, LinkedList<String> hosts) {
            mHosts.put(key, hosts);
            return this;
        }

        /**
         * 是否为调试模式 已废弃
         * @param isDebug
         * @return
         * @deprecated
         */
        public Builder setDebugable(boolean isDebug) {
            mIsDebug = isDebug;
            return this;
        }

        /**
         * 设置全局公共请求头,如果设置,则所有请求都携带此头部信息
         * @param headers
         * @return
         */
        public Builder setCommonHeaders(HashMap<String, String> headers) {
            mHeaders.putAll(headers);
            return this;
        }

        /**
         * 设置默认的Tube请求选项
         * @param options
         * @return
         */
        public Builder setDefaultOptions(TubeOptions options) {
            mDefaultOptions = options;
            return this;
        }

        /**
         * 创建并返回一个TubeConfig实例
         * @return
         */
        public TubeConfig create() {
            TubeConfig cfg = new TubeConfig();

            if (mDefaultOptions == null) {
                mDefaultOptions = new TubeOptions.Builder().create();
            }
            cfg.mHosts = mHosts;
            cfg.isDebug = mIsDebug;
            cfg.mCommonHeaders = mHeaders;
            cfg.mDefaultOptions = mDefaultOptions;

            return cfg;
        }
    }

}
