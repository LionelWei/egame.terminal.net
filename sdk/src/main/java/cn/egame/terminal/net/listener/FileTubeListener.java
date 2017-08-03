package cn.egame.terminal.net.listener;


/*
 * FileName:    FileTubeListener.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/21/16 1.00 初始版本
 */


import java.io.File;
import java.io.InputStream;

public abstract class FileTubeListener implements StreamTubeListener<File> {
    private File mFile;

    public FileTubeListener(File file) {
        mFile = file;
    }

    public File getFilePath() {
        return mFile;
    }

    // 无需重写该方法, 在parser中处理
    @Override
    public final File doInBackground(InputStream water) throws Exception {
        return null;
    }
}
