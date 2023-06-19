package tech.wedev.autm.asyntask.utils;

import java.net.InetAddress;

/**
 * 容器工具类
 */
public class ContainerUtil {
    private static String hostIp = null;//容器所载宿主机IP
    private static String containerId = null;//容器ID
    private static String httpPort = null;//容器对外http端口
    private static String httpsPort = null;//容器对外https端口
    private static String dubboPort = null;//容器对外dubbo端口

    /**
     * 工具类需添加至少一个non-public constructor,解决sonar问题
     */
    private ContainerUtil() {
        throw new IllegalStateException("ContainerUtil.class");
    }

    //加载类时加载变量，避免每次调用加载
    static {
        //获取宿主机IP
        hostIp = System.getenv("_D_HOST_IP");//获取宿主机IP
        if (hostIp == null || hostIp.trim().equals("")) {
            hostIp = null;
        }

        //获取宿主机对外http端口
        httpPort = System.getenv("HTTP9080");
        if (httpPort == null || httpPort.trim().equals("")) {
            httpPort = null;
        }

        //获取宿主机对外https端口
        httpsPort = System.getenv("HTTPS9443");
        if (httpsPort == null || httpsPort.trim().equals("")) {
            httpsPort = null;
        }

        //优先读取paas2.0环境变量，取不到时改为paas1.0做法
        dubboPort = System.getenv("_PAAS_PORT_28080");
        if (dubboPort == null || dubboPort.trim().equals("")) {
            dubboPort = System.getenv("DOCKER_DUBBO_PORT");
            if (dubboPort == null || dubboPort.trim().equals("")) {
                dubboPort = System.getenv("DOCKER_HTTP_PORT");
            }
        }

        //获取容器ID
        if (hostIp != null) {
            //优先读取paas2.0环境变量，取不到时改为paas1.0做法，从IP中截取信息
            containerId = System.getenv("_PAAS_CURRENT_CONTAINERID");
            if (containerId != null) {
                ;
            } else {
                String localhost = null;
                try {
                    localhost = InetAddress.getLocalHost().toString();//e195c275bf6e/172.17.0.13格式
                } catch (Exception e) {

                }
                if (localhost != null) {
                    containerId = localhost.substring(0, localhost.indexOf('/'));
                }
            }
        }
    }

    /**
     * 是否在容器内运行
     *
     * @return 是否在容器内运行
     */
    public static boolean isContainer() {
        return hostIp != null;
    }

    /**
     * 获取容器所在宿主机IP，如果是非容器环境，则返回null
     *
     * @return 容器所在宿主机IP
     */
    public static String getHostIp() {
        return hostIp;
    }

    /**
     * 获取容器对外http端口，如果是非容器环境，则返回null
     *
     * @return 容器对外http端口
     */
    public static String getHttpPort() {
        return httpPort;
    }

    /**
     * 获取容器对外https端口，如果是非容器环境，则返回null
     *
     * @return 容器对外https端口
     */
    public static String getHttpsPort() {
        return httpsPort;
    }

    /**
     * 获取容器对外dubbo端口，如果是非容器环境，则返回null
     *
     * @return 容器对外dubbo端口
     */
    public static String getDubboPort() {
        return dubboPort;
    }

    /**
     * 获取容器ID
     *
     * @return 容器ID
     */
    public static String getContainerID() {
        return containerId;
    }
}
