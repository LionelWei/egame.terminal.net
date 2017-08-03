package cn.egame.terminal.net.listener;


/*
 * FileName:    RawResponseListener.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/31/16 1.00 初始版本
 */


import cn.egame.terminal.net.core.TubeResponse;

public abstract class RawResponseListener implements TubeListener<Object, TubeResponse> {
    @Override
    public final TubeResponse doInBackground(Object water) throws Exception {
        return null;
    }
}
