package tech.wedev.autm.asyntask.api;

/**
 * 异步任务监控接口
 */
public interface AsynTaskMonitor {

    /**
     * 可用性监控，用于监控任务的取数线程的可用性，每隔scheduleTimeInterval调起一次
     * @param taskType 任务大类
     * @param scheduleTimeInterval 取数时间间隔
     * @param usabilityCount 第几次调用，当返回值为true的时候清除
     * @return 当发送应用监控时返回true，下次调用usabilityCount从0开始，不发送则返回false
     */
    boolean taskUsabilityMonitor(String taskType, int scheduleTimeInterval, int usabilityCount);

    /**
     * 可用性监控，用于监控任务的超时监控线程的可用性，每隔scheduleTimeInterval调起一次
     * @param taskType 任务大类
     * @param scheduleTimeInterval 超时监控时间间隔
     * @param usabilityCount 第几次调用，当返回值为true的时候清除
     * @return 当发送应用监控时返回true，下次调用usabilityCount从0开始，不发送则返回false
     */
    boolean taskTimeOutUsabilityMonitor(String taskType, int scheduleTimeInterval, int usabilityCount);

    /**
     * 监控异步任务忙碌状态，当监控到处于忙碌状态时调用
     * @param taskType 任务大类
     * @param continuousBusyCount 持续处于忙碌状态的次数
     * @param monitTimeInterval 忙碌状态监控的时间间隔（毫秒）
     */
    void taskBusyMonitor(String taskType, int continuousBusyCount, int monitTimeInterval);
}
