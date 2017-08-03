package cn.egame.terminal.net.parser;


/*
 * FileName:    BitmapParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/26/16 1.00 初始版本
 */


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;

public class BitmapParser extends StreamParser<Bitmap> {
    public BitmapParser(TubeResponse response) {
        super(response);
    }

    @Override
    public Bitmap parseResult() throws TubeException {
        return BitmapFactory.decodeStream(response.getStream());
    }
}
