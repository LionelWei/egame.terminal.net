package cn.egame.terminal.net.parser;


/*
 * FileName:    JsonParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/24/16 1.00 初始版本
 */


import org.json.JSONException;
import org.json.JSONObject;

import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.exception.TubeParseException;
import cn.egame.terminal.net.listener.TubeListener;

public class JsonParser<T> extends Parser<JSONObject, T> {
    public JsonParser(TubeResponse response) {
        super(response);
    }

    @Override
    public JSONObject parseParam() throws TubeException {
        try {
            return new JSONObject(response.getString());
        } catch (JSONException e) {
            throw new TubeParseException("JSONObject parseParam error: " + e.getLocalizedMessage());
        }
    }
}
