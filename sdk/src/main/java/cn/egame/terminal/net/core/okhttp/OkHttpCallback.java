package cn.egame.terminal.net.core.okhttp;

import cn.egame.terminal.net.core.TubeCallback;
import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import okhttp3.Callback;



/*
 * FileName:    OkHttpCallback.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/23/16 1.00 初始版本
 */


public class OkHttpCallback implements TubeCallback {
    Callback mCallback;

    public OkHttpCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onFailure(TubeException e) {


    }

    @Override
    public void onResponse(TubeResponse response) throws TubeException {

    }
}
