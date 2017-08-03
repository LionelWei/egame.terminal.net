package cn.egame.terminal.net.exception;


/*
 * FileName:    TubeIOException.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/23/16 1.00 初始版本
 */


public class TubeIOException extends TubeException {
    public TubeIOException(String info) {
        super(info);
    }

    public TubeIOException(Throwable e) {
        super(e);
    }
}
