/*
 * FileName:    NetworkAccess.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      Jh
 * Description: <文件描述>
 * History:     2015-7-28 1.00 初始版本
 */
package cn.egame.terminal.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.egame.terminal.net.core.OkHttpConnection;
import okhttp3.Response;

public class NetworkAccess {

    public static final int NETWORK_AVAILABLE = 1;
    public static final int NETWORK_UNAVAILABLE = 2;
    public static final int NETWORK_NEED_JUDGE = 3;
    private static final String TAG = "TEST_CON";

    private static final String PING_ADDRESS = "http://ping.4g.play.cn";
    private static final String DPLUS_ADDRESS = "http://119.29.29.29/d?dn=ping.4g.play.cn";

    /**
     * <功能简述> 开放接口，用于同步请求
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkResult result = hasInternetAccess(context);
        if (result == null) {
            return false;
        }
        return result.result;
    }

    /**
     * <功能简述> 开放接口，用于异步请求
     *
     * @param listener
     */
    public static boolean isNwAvailableAsyn(final NetworkListener listener,
                                            final Context context, final boolean isBackMainLooper) {
        if (listener == null) {
            return false;
        }
        new Thread("NA") {
            public void run() {
                //1 do
                final NetworkResult result = hasInternetAccess(context);

                //2 return
                if (isBackMainLooper) {
                    Handler mHandler = new Handler(Looper.getMainLooper());

                    mHandler.post(new Runnable() {

                        public void run() {

                            listener.onResult(result.result,
                                    result.result ? "OK" : result.msg);
                        }
                    });
                } else {
                    listener.onResult(result.result,
                            result.result ? "OK" : result.msg);
                }
            }

        }.start();
        return false;
    }

    /**
     * <功能简述> 判定网络通断
     * 使用Head请求ping.4g.play.cn，若返回状态码为301或302，进入isUrlMatch(),
     * 若状态码不正确或抛出不可判定异常，进入dPlus()
     *
     * @param context
     * @return
     */
    private static NetworkResult hasInternetAccess(Context context) {
        NetworkResult networkResult = new NetworkResult();
        if (isConnAvailable(context)) {
            Response response = null;
            try {
                response = new OkHttpConnection.Builder()
                        .url(PING_ADDRESS)
                        .build()
                        .execute();
                String location = response.header("Location");
                int statusCode = response.code();
                if (statusCode == 301 || statusCode == 302) {
                    if (location != null) {
                        if (isUrlMatch(location)) {
                            networkResult.result = true;
                            return networkResult;
                        } else {
                            dPlus(networkResult);
                            return networkResult;
                        }
                    }
                }
            } catch (IOException e) {
                ELog.e(TAG, "Error checking internet connection", e);
                switch (exceptionFilter(e.getMessage(), networkResult)) {
                    case NETWORK_AVAILABLE:
                        return networkResult;
                    case NETWORK_UNAVAILABLE:
                        return networkResult;
                    case NETWORK_NEED_JUDGE:
                        dPlus(networkResult);
                        return networkResult;
                    default:
                        break;
                }
            } finally {
                if (response != null && response.body() != null) {
                    response.body().close();
                }
            }
        } else {
            ELog.d(TAG, "No network available!");
            networkResult.result = false;
            networkResult.msg = "No network available!";
        }
        return networkResult;
    }

    /**
     * <功能简述> 判定网络开关是否打开
     *
     * @param context
     * @return
     */
    public static boolean isConnAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <功能简述> D+验证
     * 使用Get方法请求D+指定路径，若返回状态码为200，
     * 且返回消息中只包含用分号隔开的ip地址，则判定验证通过。
     *
     * @return
     */
    private static void dPlus(NetworkResult networkResult) {
        do {
            Response response = null;
            try {
                response = new OkHttpConnection.Builder()
                        .url(DPLUS_ADDRESS)
                        .build()
                        .execute();
                String hostIp = response.body().string();

                if (response.code() != 200) {
                    networkResult.msg = "d+ status code error";
                    networkResult.result = false;
                    break;
                }

                if (TextUtils.isEmpty(hostIp)) {
                    networkResult.msg = "hostIp is empty";
                    networkResult.result = false;
                    break;
                }

                String[] list = hostIp.split(";");

                if (list == null || list.length == 0) {
                    networkResult.msg = "hostIp error";
                    networkResult.result = false;
                    break;
                }

                for (String hostIpStr : list) {
                    if (!isIpv4(hostIpStr)) {
                        networkResult.msg = "Ipv4 error";
                        networkResult.result = false;
                        break;
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                networkResult.msg = e1.getMessage();
                networkResult.result = false;
                break; //使用D+请求抛出IO异常，直接判定网络不通ͨ
            } finally {
                if (response != null && response.body() != null) {
                    response.body().close();
                }
            }

        } while (false);
    }

    /**
     * <功能简述> 异常筛选
     * 与ping.4g.play.cn进行连接时，若抛出指定异常，不认为网络不通。
     *
     * @param e
     * @return
     */
    private static int exceptionFilter(String e, NetworkResult result) {
        // TODO Auto-generated method stub
        if (e == null) {
            return NETWORK_UNAVAILABLE;
        }

        //        putErrorMsg(e);
        int indexReset = e.toUpperCase().indexOf("ECONNRESET");
        int indexPipe = e.toUpperCase().indexOf("EPIPE");
        int indexTarget = e.toLowerCase()
                .indexOf("the target server failed to respond");

        int indexTimeOut = e.toLowerCase().indexOf("timed out");
        int indexRefused = e.toLowerCase().indexOf("refused");
        int indexNetUnreach = e.toLowerCase().indexOf("network unreachable");
        int indexNoRoute = e.toLowerCase().indexOf("no route to host");

        if (indexReset != -1 || indexPipe != -1 || indexTarget != -1) {
            result.msg = e;
            result.result = true;
            return NETWORK_AVAILABLE;
        } else if (indexTimeOut != -1 || indexRefused != -1
                || indexNetUnreach != -1 || indexNoRoute != -1) {
            result.msg = e;
            result.result = false;
            return NETWORK_UNAVAILABLE;
        } else { //需要二次判定的异常：1) No address associated with hostname; 2) The socket level is invalid
            result.msg = e;
            result.result = false;
            return NETWORK_NEED_JUDGE;
        }
    }

    /**
     * <功能简述> URL匹配
     * 使用正则表达式从url中提取Host,并判断Host是否以play.cn结尾
     *
     * @param url
     * @return
     */
    public static boolean isUrlMatch(String url) {
        if (url == null) {
            return false;
        }
        if (!url.toLowerCase().startsWith("http://")
                && !url.toLowerCase().startsWith("https://")) {
            url = "http://" + url;
        }

        String host = "";
        try {
            Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
            Matcher m = p.matcher(url);
            if (m.find()) {
                host = m.group();
            }

        } catch (Exception e) {
        }
        if (host.toLowerCase().endsWith(".html")
                || host.toLowerCase().endsWith(".htm")) {
            host = "";
        }
        return host.toLowerCase().endsWith("play.cn");

    }

    /**
     * <功能简述> 验证Ip地址
     *
     * @param ipAddress
     * @return
     */
    private static boolean isIpv4(String ipAddress) {

        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();

    }

    public interface NetworkListener {
        /**
         * <功能简述> 自定义Listener，用于异步判断网络通断
         * 请求成功后，将网络通断标志量及相关信息返回给调用者
         *
         * @param isNetworkOk
         * @param exception
         */
        void onResult(Boolean isNetworkOk, String exception);
    }

    /**
     * <功能简述> 返回网络判断结果的静态内部类
     * <功能详细描述>
     *
     * @author Administrator
     */
    public static class NetworkResult {
        public boolean result = true;
        public String msg = null;
    }

}
