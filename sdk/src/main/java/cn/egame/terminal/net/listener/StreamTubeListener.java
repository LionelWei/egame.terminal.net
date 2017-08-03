/*
 * FileName:	StreamTubeListener.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2014-8-26 1.00 初始版本
 */
package cn.egame.terminal.net.listener;

import java.io.InputStream;

/**
 * Http请求返回类型是Stream的接口
 *
 * @author Hein
 */
public interface StreamTubeListener<Result> extends
        TubeListener<InputStream, Result> {
}
