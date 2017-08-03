package cn.egame.terminal.net.parser;


/*
 * FileName:    StringParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/24/16 1.00 初始版本
 */


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.GsonModelTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;

public class StringParser<T> extends Parser<String, T> {

    @SuppressWarnings({ "unchecked"})
    public static <T> Parser<String, T> createBy(TubeResponse response,
                                                 StringTubeListener<T> listener) {
        if (listener instanceof GsonModelTubeListener) {
            Type type  = ((ParameterizedType)listener.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            return new GsonModelParser<>(response, type);
        }
        return new StringParser<>(response);
    }

    public StringParser(TubeResponse response) {
        super(response);
    }

    @Override
    public String parseParam() throws TubeException {
        return response.getString();
    }
}
