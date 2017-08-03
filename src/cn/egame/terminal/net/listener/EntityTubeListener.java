/*
 * FileName:	StreamTubeListener.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2014-8-26 1.00 初始版本
 */
package cn.egame.terminal.net.listener;

import org.apache.http.HttpEntity;

import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;

/**
 * Http请求返回类型是Stream的接口
 *
 * @author Hein
 */
public abstract class EntityTubeListener<Result> implements
        TubeListener<HttpEntity, Result> {

    public boolean prepare(String url, TubeOptions opt) {
        return true;
    }
}
