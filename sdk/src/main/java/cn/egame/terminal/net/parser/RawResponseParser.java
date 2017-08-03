package cn.egame.terminal.net.parser;


/*
 * FileName:    RawResponseParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/31/16 1.00 初始版本
 */


import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;

public class RawResponseParser extends Parser<Object, TubeResponse> {
    public RawResponseParser(TubeResponse tubeResponse) {
        super(tubeResponse);
    }

    @Override
    public Object parseParam() throws TubeException {
        return null;
    }

    @Override
    public TubeResponse parseResult() throws TubeException {
        return response;
    }
}
