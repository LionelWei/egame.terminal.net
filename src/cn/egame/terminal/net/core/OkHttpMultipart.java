package cn.egame.terminal.net.core;

/*
 * FileName:	OkHttpMultipart.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		weilai
 * Description:	拼接成适用于okhttp的multipart
 * History:		2016/6/2 1.00 初始版本
 */

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class OkHttpMultipart {
    public static RequestBody getRequestBody(MultipartFormData formData) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 设置form-data格式
        builder.setType(MultipartBody.FORM);

        // 文本拼接
        final Map<String, MultipartFormData.TextBody> textForm = formData.getTextForm();
        for (Map.Entry<String, MultipartFormData.TextBody> entry : textForm.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue().value());
        }

        // 二进制/文件拼接
        final Map<String, MultipartFormData.BinaryBody> fileForm = formData.getFileForm();
        for (Map.Entry<String, MultipartFormData.BinaryBody> entry : fileForm.entrySet()) {
            RequestBody requestBody;
            if (entry.getValue().isFile()) {
                requestBody = RequestBody.create(MediaType.parse(entry.getValue().mediaType()),
                        new File(entry.getValue().filePath()));
            } else {
                requestBody = RequestBody.create(MediaType.parse(entry.getValue().mediaType()),
                        entry.getValue().binary());
            }
            builder.addFormDataPart(entry.getKey(), entry.getValue().fileName(), requestBody);
        }

        return builder.build();
    }
}
