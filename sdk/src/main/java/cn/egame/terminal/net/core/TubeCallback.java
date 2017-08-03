package cn.egame.terminal.net.core;


/*
 * FileName:    TubeCallback.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/23/16 1.00 初始版本
 */


import cn.egame.terminal.net.exception.TubeException;

public interface TubeCallback {
    void onFailure(TubeException e);

    void onResponse(TubeResponse response) throws TubeException;
}
