package cn.egame.terminal.net.parser;


/*
 * FileName:    Parser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/24/16 1.00 初始版本
 */


import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.RawResponseListener;
import cn.egame.terminal.net.listener.StreamTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;
import cn.egame.terminal.net.listener.TubeListener;

public class Parser<P, T> {
    protected TubeResponse response;

    // response主要分为三大类:
    // Raw, String和Stream
    // 其子类放在各自的parse里面再单独处理
    // 如String可包含Json, XML; Stream包含File, Bitmap等
    // 之所以把JSONTubeListener放在这里, 是为了兼容以前代码, 其实没有必要
    @SuppressWarnings({ "unchecked"})
    public static <P, T> Parser<P, T> create(TubeResponse response,
                                             TubeListener<P, T> listener) {
        if (listener instanceof RawResponseListener) {
            return (Parser<P, T>) new RawResponseParser(response);
        } else if (listener instanceof StringTubeListener) {
            return StringParser.createBy(response, (StringTubeListener) listener);
        } else if (listener instanceof StreamTubeListener) {
            return StreamParser.createBy(response, (StreamTubeListener) listener);
        } else if (listener instanceof JSONTubeListener) {
            return (Parser<P, T>) new JsonParser<>(response);
        }
        return new Parser<>(response);
    }

    protected Parser(TubeResponse tubeResponse) {
        response = tubeResponse;
    }

    // 默认返回String
    public P parseParam() throws TubeException {
        return null;
    }

    public T parseResult() throws TubeException {
        return null;
    }

}
