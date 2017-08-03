package cn.egame.terminal.net.parser;


/*
 * FileName:    StreamParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/24/16 1.00 初始版本
 */


import java.io.InputStream;

import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.BitmapTubeListener;
import cn.egame.terminal.net.listener.FileTubeListener;
import cn.egame.terminal.net.listener.StreamTubeListener;

public class StreamParser<T> extends Parser<InputStream, T> {
    @SuppressWarnings({ "unchecked"})
    public static <T> StreamParser<T> createBy(TubeResponse response,
                                                      StreamTubeListener<T> listener) {
        if (listener instanceof BitmapTubeListener) {
            return (StreamParser<T>) new BitmapParser(response);
        } else if (listener instanceof FileTubeListener) {
            return (StreamParser<T>) new FileParser(
                    response, ((FileTubeListener) listener).getFilePath());
        }
        return new StreamParser<>(response);
    }

    public StreamParser(TubeResponse response) {
        super(response);
    }

    @Override
    public InputStream parseParam() throws TubeException {
        return response.getStream();
    }
}
