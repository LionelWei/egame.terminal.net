package cn.egame.terminal.net.core;

/*
 * FileName:	EntityResult.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		weilai
 * Description:	<文件描述>
 * History:		2016/5/17 1.00 初始版本
 */

import java.io.InputStream;

/**
 * 对返回的entity进行封装
 *
 */
public class EntityResult {
    private TubeResponse mResponse = null;
    /**
     * 唯一构造方法，使用response构造
     */
    public EntityResult(TubeResponse response) {
        mResponse = response;
    }

    public String entity2String() {
        return mResponse != null
                ? mResponse.getString()
                : null;
    }

    public InputStream entity2Stream() {
        return mResponse != null
                ? mResponse.getStream()
                : null;
    }

    /**
     * 关闭此次连接并释放资源
     */
    public void close() {
        if (mResponse != null) {
            mResponse.close();
        }
    }
}
