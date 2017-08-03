package cn.egame.terminal.net.parser;


/*
 * FileName:    GsonModelParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/26/16 1.00 初始版本
 */


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.exception.TubeParseException;

public class GsonModelParser<T> extends StringParser<T> {
    private Type mType;

    /*package*/ GsonModelParser(TubeResponse response, Type type) {
        super(response);
        mType = type;
    }

    @Override
    public T parseResult() throws TubeException {
        if (!checkExist()) {
            throw new TubeException("Gson isn't imported");
        }
        Gson gson = new Gson();
        try {
            return gson.fromJson(response.getString(), mType);
        } catch (JsonSyntaxException e) {
            throw new TubeParseException("Gson parseParam error: " + e.getLocalizedMessage());
        }

    }

    private boolean checkExist() {
        try {
            Class.forName("com.google.gson.Gson");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
