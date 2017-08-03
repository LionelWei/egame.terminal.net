/*
 * FileName:	TubeException.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.exception;

import java.io.IOException;

/**
 *  </Br>
 * <功能详细描述> </Br>
 * 
 * @author  Hein
 */
public class TubeException extends IOException {
    public TubeException(String info) {
        super(info);
    }

    public TubeException(Throwable throwable) {
        super(throwable);
    }
}
