package cn.egame.terminal.net.core.okhttp.interceptors;

import java.io.IOException;
import java.util.Locale;

import cn.egame.terminal.net.listener.TubeListener;
import cn.egame.terminal.utils.ELog;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
 * FileName:	PreparationInterceptor.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		weilai
 * Description:	http请求前的准备工作
 * History:		2016/5/30 1.00 初始版本
 */

public class PreparationInterceptor implements Interceptor {
    private final static String TAG = "Interceptor";

    private TubeListener<?, ?> mListener;
    public PreparationInterceptor(TubeListener<?, ?> listener) {
        mListener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        StringBuilder log = new StringBuilder();
        long t1 = System.nanoTime();

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        log.append(String.format(Locale.getDefault(), "----Response in %.1fms", (t2 - t1) / 1e6d));
        if (ELog.LOG_ON) {
            ELog.v(TAG, "\n" + log.toString());
        }
        return response;
    }
}
