/*
 * FileName:	StringTubeListener.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.listener;

import cn.egame.terminal.net.core.TubeOptions;

/**
 * Http请求返回类型是String的接口
 *
 * @author Hein
 */
public abstract class StringTubeListener<Result> implements
        TubeListener<String, Result> {

    public boolean prepare(String url, TubeOptions opt) {
        return true;
    }
}
