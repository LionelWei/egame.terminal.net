/*
 * FileName:	TubeListener.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.listener;

import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.exception.TubeException;

/**
 * 
 * 一般的请求头接口
 * 
 * @author Hein
 */
public interface TubeListener<Params, Result> {

    boolean prepare(String url, TubeOptions opt);

    /**
     * 
     * 请求成功后将得到的数据以指定的形式返回给调用者处理
     * 
     * @param water
     * @return
     */
    Result doInBackground(Params water) throws Exception;

    /**
     * 
     * 数据处理后返回，返回的线程是构造时的所在线程
     * 
     * @param result
     */
    void onSuccess(Result result);

    /**
     * 
     * 
     * @param e
     */
    void onFailed(TubeException e);
}
