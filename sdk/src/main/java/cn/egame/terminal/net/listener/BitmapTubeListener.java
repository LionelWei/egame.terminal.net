package cn.egame.terminal.net.listener;


/*
 * FileName:    BitmapTubeListener.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/21/16 1.00 初始版本
 */


import android.graphics.Bitmap;

import java.io.InputStream;

public abstract class BitmapTubeListener implements StreamTubeListener<Bitmap>  {

    // 无需重写该方法, 在parser中处理
    @Override
    public final Bitmap doInBackground(InputStream water) throws Exception {
        return null;
    }
}
