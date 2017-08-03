package cn.egame.terminal.net.data;

/*
 * FileName:	MultipartFormData.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		weilai
 * Description:	创建独立于协议栈的Multipart/form-data, 以降低耦合度
 * History:		2016/6/2 1.00 初始版本
 */

import java.util.HashMap;
import java.util.Map;

public class MultipartFormData {
    private Map<String, TextBody> mTextForm = new HashMap<String, TextBody>();
    private Map<String, BinaryBody> mFileForm = new HashMap<String, BinaryBody>();

    private MultipartFormData(Map<String, TextBody> textForm, Map<String, BinaryBody> fileForm) {
        mTextForm = textForm;
        mFileForm = fileForm;
    }

    public final Map<String, TextBody> getTextForm() {
        return mTextForm;
    }

    public final Map<String, BinaryBody> getFileForm() {
        return mFileForm;
    }

    public static class Builder {
        private Map<String, TextBody> mTextForm = new HashMap<String, TextBody>();
        private Map<String, BinaryBody> mFileForm = new HashMap<String, BinaryBody>();

        // 上传文字
        public Builder addTextPart(String name, String value) {
            mTextForm.put(name, new TextBody(value));
            return this;
        }

        // 上传文件
        public Builder addFilePart(String name, String fileName,
                                             String mediaType, String filePath) {
            mFileForm.put(name, new BinaryBody(fileName, mediaType, filePath));
            return this;
        }

        // 上传二进制
        public Builder addFilePart(String name, String fileName,
                                   String mediaType, byte[] binary) {
            mFileForm.put(name, new BinaryBody(fileName, mediaType, binary));
            return this;
        }

        public MultipartFormData build() {
            return new MultipartFormData(mTextForm, mFileForm);
        }
    }

    public static class TextBody {
        private String mValue;

        public TextBody(String value) {
            mValue = value;
        }

        public String value() {
            return mValue;
        }

    }

    public static class BinaryBody {
        private String mFileName;
        private String mMediaType;
        private String mFilePath;
        private byte[] mBinary;
        private boolean mIsFile;

        public BinaryBody(String fileName, String mediaType, String filePath) {
            mFileName = fileName;
            mMediaType = mediaType;
            mFilePath = filePath;
            mIsFile = true;
        }

        public BinaryBody(String fileName, String mediaType, byte[] binary) {
            mFileName = fileName;
            mMediaType = mediaType;
            mBinary = binary;
            mIsFile = false;
        }

        public String fileName() {
            return mFileName;
        }

        public String mediaType() {
            return mMediaType;
        }

        public String filePath() {
            return mFilePath;
        }

        public byte[] binary() {
            return mBinary;
        }

        public boolean isFile() {
            return mIsFile;
        }
    }

}
