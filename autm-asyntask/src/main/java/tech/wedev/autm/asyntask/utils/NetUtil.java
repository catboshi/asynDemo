package tech.wedev.autm.asyntask.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络相关工具
 */
public class NetUtil {
    public static final int IPv4 = 0x0000;//IPv4
    public static final int IPv6 = 0x0001;//IPv6
    public static final String UNKNOWN_IP = "0.0.0.0";//未知IP
    public static final String UNKNOWN_MAC = "00-00-00-00-00-00";//未知MAC

    private static String ip;//IP地址，仅获取一次
    private static String unLoopbackIp;//非局域网、IPv4第一块网卡地址，仅获取一次
    private static String mac;//MAC地址，仅获取一次
    private static String unLoopbackMac;//第一块网卡地址，仅获取一次

    /**
     * 工具类需添加至少一个non-public constructor,解决sonar问题
     */
    private NetUtil() {
        throw new IllegalStateException("NetUtil.class");
    }

    /**
     * 获取本机IP地址
     * 当出现多块网卡或localhost有回环定义时，返回可能不准确，如127.0.0.1
     *
     * @return IP地址，如果获取失败，返回"0.0.0.0"
     */
    private static String getLocalhost() {
        if (ip == null) {
            synchronized (NetUtil.class) {
                if (ip == null) {
                    ip = doGetLocalhost();
                }
            }
        }
        return ip;
    }

    /**
     * 获取本机IP地址
     * 当出现多块网卡或localhost有回环定义时，返回可能不准确，如127.0.0.1
     *
     * @return
     */
    private static String doGetLocalhost() {
        try {
            StringBuilder buffer = new StringBuilder();
            byte[] ipbs = InetAddress.getLocalHost().getAddress();
            for (byte b : ipbs) {
                buffer.append((b < 0 ? b + 256 : b) + ".");
            }
            return buffer.deleteCharAt(buffer.length() - 1).toString();
        } catch (UnknownHostException e) {
            return UNKNOWN_IP;
        }
    }

    /**
     * 获取本机IP地址
     * 当有多块网卡时，仅返回第一块网卡IP，排除IPv6以及192.168.开头的局域网IP
     *
     * @return IP地址，如果获取失败，返回"0.0.0.0"
     */
    public static String getUnLoopbackLocalhost() {
        if (unLoopbackIp == null) {
            synchronized ((NetUtil.class)) {
                if (unLoopbackIp == null) {
                    unLoopbackIp = doGetUnLoopbackLocalhost();
                }
            }
        }
        return unLoopbackIp;
    }

    /**
     * 获取本机IP地址
     * 当有多块网卡时，仅返回第一块网卡IP
     *
     * @return IP地址，如果获取失败，返回"0.0.0.0"
     */
    public static String doGetUnLoopbackLocalhost() {
        try {
            Enumeration<NetworkInterface> addrs = NetworkInterface
                    .getNetworkInterfaces();
            while (addrs.hasMoreElements()) {
                NetworkInterface ni = addrs.nextElement();
                if (!ni.isLoopback() && ni.isUp()) {
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        String host = ips.nextElement().getHostAddress();
                        if (host.indexOf(':') == -1
                                && host.indexOf("192.168.") == -1
                                && !host.equals("0.0.0.0")
                                && !host.equals("127.0.0.1")) {
                            return host;
                        }
                    }
                }
            }
            return getLocalhost();
        } catch (SocketException e) {
            return getLocalhost();
        }
    }
}
