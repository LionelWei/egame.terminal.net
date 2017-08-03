package cn.egame.terminal.net.exception;


/*
 * FileName:    TubeParseException.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/23/16 1.00 初始版本
 */


public class TubeParseException extends TubeException {
    public TubeParseException(String info) {
        super(info);
    }

    public TubeParseException(Throwable e) {
        super(e);
    }
}
