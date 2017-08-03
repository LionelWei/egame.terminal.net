package cn.egame.terminal.net.listener;


/*
 * FileName:    GsonModelTubeListener.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/26/16 1.00 初始版本
 */


public abstract class GsonModelTubeListener<Result> implements StringTubeListener<Result> {

    // 无需重写该方法, 在parser中处理
    @Override
    public final Result doInBackground(String water) throws Exception {
        return null;
    }
}
