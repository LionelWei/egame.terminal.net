package cn.egame.terminal.net.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import cn.egame.terminal.net.utils.ByteArrayBuilder;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpResponse {

    public static String getString(Response response) {
        try {
            return doGetString(response);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static InputStream getStream(Response response) {
        return response.body().byteStream();
    }

    private static String doGetString(Response response) throws IOException{
        ResponseBody responseBody = null;
        InputStream is = null;
        GZIPInputStream gzipInputStream = null;
        try {
            if (!isGzip(response)) {
                return response.body().string();
            } else {
                responseBody = response.body();
                is = responseBody.byteStream();
                gzipInputStream = new GZIPInputStream(is);
                return getStringFromStream(gzipInputStream);
            }
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
            if (is != null) {
                is.close();
            }
            if (gzipInputStream != null) {
                gzipInputStream.close();
            }
        }
    }

    private static boolean isGzip(Response response) {
        boolean isGzip = false;
        String acceptEncoding = response.header("Accept-Encoding");
        if (acceptEncoding != null) {
            if (acceptEncoding.equals("gzip")) {
                isGzip = true;
            }
        }
        return isGzip;
    }

    private static String getStringFromStream(InputStream inputStream) throws IOException{
        ByteArrayBuilder dataBuilder = new ByteArrayBuilder();

        byte[] buf = null;
        int count = 0;

        /*
         * accumulate enough data to make it worth pushing it up the stack
         */
        buf = new byte[8 * 1024];
        int len = 0;
        int lowWater = buf.length / 2;

        while (len != -1) {

            len = inputStream.read(buf, count, buf.length - count);

            if (len != -1) {
                count += len;
            }
            if (len == -1 || count >= lowWater) {
                dataBuilder.append(buf, 0, count);
                count = 0;
            }
        }

        byte[] data = new byte[dataBuilder.getByteSize()];
        int offset = 0;
        while (true) {
            ByteArrayBuilder.Chunk c = dataBuilder.getFirstChunk();
            if (c == null)
                break;

            if (c.mLength != 0) {
                System.arraycopy(c.mArray, 0, data, offset, c.mLength);
                offset += c.mLength;
            }
            c.release();
        }
        return new String(data);
    }
}
