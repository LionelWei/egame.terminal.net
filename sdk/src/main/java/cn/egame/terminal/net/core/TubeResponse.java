package cn.egame.terminal.net.core;


/*
 * FileName:    TubeResponse.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: 参照OkHttp的Response, 设计独立于协议栈的response接口
 * History:     10/23/16 1.00 初始版本
 */


import java.io.InputStream;

public interface TubeResponse {
    String header(String name);

    int code();

    String getString();

    InputStream getStream();

    void close();
}