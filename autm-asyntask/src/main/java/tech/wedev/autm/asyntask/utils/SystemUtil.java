package tech.wedev.autm.asyntask.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * 系统类工具
 */
public class SystemUtil {

    //操作系统信息获取的MBean
    private static final OperatingSystemMXBean OS = ManagementFactory.getOperatingSystemMXBean();

    //JVM信息获取的MBean
    private static final RuntimeMXBean VM = ManagementFactory.getRuntimeMXBean();

    /**
     * 工具类需添加至少一个non-public constructor,解决sonar问题
     */
    private SystemUtil() {
        throw new IllegalStateException("SystemUtil.class");
    }

    /**
     * 获取系统IP，如果为容器，则获取宿主机IP；如果非容器，则获取机器上第一块网卡IP
     */
    public static String getHostIp() {
        if (ContainerUtil.isContainer()) {
            return ContainerUtil.getHostIp();
        } else {
            return NetUtil.getUnLoopbackLocalhost();
        }
    }

    /**
     * 获取系统IP，如果为容器，则获取容器ID；如果非容器，则获取机器上第一块网卡IP
     *
     * @return 系统IP或容器ID
     */
    public static String getContainerIdOrHostIp() {
        if (ContainerUtil.isContainer()) {
            return ContainerUtil.getContainerID();
        } else {
            return NetUtil.getUnLoopbackLocalhost();
        }
    }

    /**
     * 获取操作系统名称
     *
     * @return 操作系统名称
     */
    public static String getOSName() {
        return OS.getName();
    }

    /**
     * 获取操作系统版本
     *
     * @return 操作系统版本
     */
    public static String getOSVersion() {
        return OS.getVersion();
    }

    /**
     * 获取CPU个数
     *
     * @return CPU个数
     */
    public static int getCpuCount() {
        return OS.getAvailableProcessors();
    }

    /**
     * 获取JVM能使用的最大内存
     *
     * @return JVM能使用的最大内存
     */
    public static long getMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获取JVM名称
     *
     * @return JVM名称
     */
    public static String getVmName() {
        return VM.getVmName();
    }

    /**
     * 获取JVM版本号
     *
     * @return JVM版本号
     */
    public static String getVmVersion() {
        return VM.getVmVersion();
    }
}
