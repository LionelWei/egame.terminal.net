package cn.egame.terminal.net.core.okhttp;

import java.io.IOException;
import java.io.InputStream;

import cn.egame.terminal.net.core.TubeResponse;
import okhttp3.Response;



/*
 * FileName:    OkHttpResponse.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/23/16 1.00 初始版本
 */


public class OkHttpResponse implements TubeResponse {
    private Response mResponse;

    public OkHttpResponse(Response response) {
        mResponse = response;
    }

    @Override
    public String header(String name) {
        return mResponse.header(name);
    }

    @Override
    public int code() {
        return mResponse.code();
    }

    @Override
    public String getString() {
        try {
            if (mResponse != null && mResponse.body() != null) {
                // by JakeWharton: "Accept-Encoding: gzip"
                // 会被自动添加到请求中, 并且在回复时自动解压缩
                return mResponse.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public InputStream getStream() {
        if (mResponse != null && mResponse.body() != null) {
            return mResponse.body().byteStream();
        }
        return null;
    }

    @Override
    public void close() {
        if (mResponse != null && mResponse.body() != null) {
            mResponse.body().close();
        }
    }
}
