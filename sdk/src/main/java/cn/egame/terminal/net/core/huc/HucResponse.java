package cn.egame.terminal.net.core.huc;

import java.io.InputStream;

import cn.egame.terminal.net.core.TubeResponse;



/*
 * FileName:    HucResponse.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/23/16 1.00 初始版本
 */


public class HucResponse implements TubeResponse {
    @Override
    public String header(String name) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int code() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getString() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public InputStream getStream() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void close() {
        throw new RuntimeException("Not implemented");
    }
}
