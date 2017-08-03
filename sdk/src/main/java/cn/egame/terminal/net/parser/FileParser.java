package cn.egame.terminal.net.parser;


/*
 * FileName:    FileParser.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     10/26/16 1.00 初始版本
 */


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.exception.TubeIOException;

public class FileParser extends StreamParser<File> {
    private File mFile;
    public FileParser(TubeResponse response, File file) {
        super(response);
        mFile = file;
    }

    @Override
    public File parseResult() throws TubeException {
        try {
            OutputStream os = new FileOutputStream(mFile);
            InputStream in = response.getStream();
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            in.close();
        } catch (IOException e) {
            throw new TubeIOException(
                    "Error when read/write file " + e.getLocalizedMessage());
        }
        return mFile;
    }
}
