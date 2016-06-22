package cn.egame.terminal.net.core;

/*
 * FileName:	EntityResult.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		weilai
 * Description:	<文件描述>
 * History:		2016/5/17 1.00 初始版本
 */

import java.io.InputStream;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 对返回的entity进行封装
 *
 */
public class EntityResult {
    private Response mResponse = null;
    /**
     * 唯一构造方法，使用response构造
     */
    public EntityResult(Response response) {
        mResponse = response;
    }

    public String entity2String() {
        if (mResponse != null) {
            OkHttpConnection.getString(mResponse);
        }
        return null;
    }

    public InputStream entity2Stream() {
        if (mResponse != null) {
            return OkHttpConnection.getStream(mResponse);
        }
        return null;
    }

    /**
     * 关闭此次连接并释放资源
     */
    public void close() {
        if (mResponse != null) {
            ResponseBody responseBody = mResponse.body();
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }
}
