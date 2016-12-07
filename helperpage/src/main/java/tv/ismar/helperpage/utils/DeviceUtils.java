package tv.ismar.helperpage.utils;

import android.content.Context;
import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import tv.ismar.app.core.VodUserAgent;


/**
 * Created by huaijie on 14-10-30.
 */
public class DeviceUtils {

    public static String getSnCode() {
        return Build.SERIAL;
    }

    public static String getLocalIpAddressV4() {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())  //这里做了一步IPv4的判定
                    {
                        ip = inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            return "0.0.0.0";
        }
        return ip;
    }

    public static String getLocalMacAddress(Context context) {
        String strMacAddr = null;
        try {
            InetAddress ip = getLocalInetAddress();

            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append('-');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strMacAddr;
    }


    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }
                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }


    public static String getModel() {
        return VodUserAgent.getModelName();
    }

    public static String getAppCacheDirectory(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }


    public static String ipToHex() {

        String ipAddress = DeviceUtils.getLocalIpAddressV4();

        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = ipAddress.indexOf(".");
        int position2 = ipAddress.indexOf(".", position1 + 1);
        int position3 = ipAddress.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(ipAddress.substring(0, position1));
        ip[1] = Long.parseLong(ipAddress.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(ipAddress.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(ipAddress.substring(position3 + 1));
        long ipAddressLong = (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
        return Long.toHexString(ipAddressLong);
    }


}
