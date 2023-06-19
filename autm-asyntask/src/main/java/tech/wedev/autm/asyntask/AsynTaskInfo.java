package tech.wedev.autm.asyntask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskStateType;
import tech.wedev.autm.asyntask.api.AbstractAsynTaskServiceFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 异步任务信息
 */
public class AsynTaskInfo {

    //与asyn_task_def表对应
    private String taskImpl;//任务实现类
    private String taskType;//任务类型
    private String taskName;//任务名称
    private int executeTaskDelay = 0;//执行任务延迟

    private String logConfig;//日志文件名，log4j.xml里面配的名字
    private Logger log = LoggerFactory.getLogger(AsynTaskInfo.class);//改为log4j

    private int getTaskSleepTime = 5 * 1000;//取任务间隔时间
    private int getTaskNum = 20;//每次取任务个数
    private String getTaskType;//取任务类型
    private String getTaskThreadName = "AsynGetTaskThread";//取任务线程名

    private int serverCorePoolSize;//最小线程数
    private int serverMaxPoolSize;//最大线程数
    private long serverKeepAliveTime = 300;//空闲线程保留时间
    private int serverQueueLimit;//队列阈值
    private String serverName = "";//服务线程池名
    private int redoNum = 3;
    private int redoTimeSplit = 0;//重做间隔，默认为0 单位秒

    private String getTimeoutTaskThreadName = "AsynGetTimeoutTaskThread";//取超时任务线程名
    private int timeoutLimit = 600;//超时时间限制 单位秒
    private int getTimeoutTaskSleepTime = 60 * 1000;//取超时任务间隔时间

    private String getTaskMapperName = "AsynTaskMapper.getTaskList";//抓任务mybatis可自行扩展
    private String getTimeoutTaskMapperName = "AsynTaskMapper.getMyIpTimeoutTaskList";//抓超时任务任务可自行扩展
    private String updTaskMapperName = "AsynTaskMapper.updateStateBeforeGetTask";//抢任务mybatis可自行扩展

    //存储线程池中的每个线程的线程编号和线程的对应关系
    private Map<String, Thread> threadMap = new HashMap<String, Thread>();

    /**
     * 添加任务，任务编号自动生成，必输字段keyword priority refCol1等 可以改为调用
     * AsynTaskThreadPoolManager
     *
     * @param taskBean
     * @return
     * @throws Throwable
     */
    public int addAsynTask(AsynTaskBean taskBean) throws Throwable {
        taskBean.setTimeoutLimit(new BigDecimal(this.timeoutLimit));
        if (taskBean.getDealNum() == null) {
            taskBean.setDealNum(new BigDecimal(1));
        }
        taskBean.setTaskType(this.getTaskType);
        if (taskBean.getState() == null || "".equals(taskBean.getState())) {
            taskBean.setState(TaskStateType.PEDDING.getCode());//待处理
        }

        return (Integer) AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().insertAsynTask(taskBean);
    }

    /**
     * 抢任务，可重载此方法进行更细致的任务抢占
     *
     * @param updMap
     * @return
     */
    public int robTask(Map<String, Object> updMap) {
        try {
            return AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService()
                    .updateStateWhenRobTask((String) updMap.get("ip"), (String) updMap.get("taskType"), (Integer) updMap.get("getTaskNum"));
        } catch (Throwable e) {
            log.error(getGetTaskThreadName() + "抢任务异常，更新条件: " + updMap, e);
            return 0;
        }
    }

    public void addThreadMap(String threadId, Thread t) {
        threadMap.put(threadId, t);
    }

    public void clearThreadMap() {
        threadMap.clear();
    }

    public Thread getThread(String threadName) {
        if (threadMap.containsKey(threadName)) {
            return threadMap.get(threadName);
        } else {
            return null;
        }
    }

    public String getTaskImpl() {
        return taskImpl;
    }

    public void setTaskImpl(String taskImpl) {
        this.taskImpl = taskImpl;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getExecuteTaskDelay() {
        return executeTaskDelay;
    }

    public void setExecuteTaskDelay(int executeTaskDelay) {
        this.executeTaskDelay = executeTaskDelay;
    }

    public String getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(String logConfig) {
        this.logConfig = logConfig;
    }

    public Logger getLog() {
        return log;
    }

    public int getGetTaskSleepTime() {
        return getTaskSleepTime;
    }

    public void setGetTaskSleepTime(int getTaskSleepTime) {
        this.getTaskSleepTime = getTaskSleepTime;
    }

    public int getGetTaskNum() {
        return getTaskNum;
    }

    public void setGetTaskNum(int getTaskNum) {
        this.getTaskNum = getTaskNum;
    }

    public String getGetTaskType() {
        return getTaskType;
    }

    public void setGetTaskType(String getTaskType) {
        this.getTaskType = getTaskType;
    }

    public String getGetTaskThreadName() {
        return getTaskThreadName;
    }

    public void setGetTaskThreadName(String getTaskThreadName) {
        this.getTaskThreadName = getTaskThreadName;
    }

    public int getServerCorePoolSize() {
        return serverCorePoolSize;
    }

    public void setServerCorePoolSize(int serverCorePoolSize) {
        this.serverCorePoolSize = serverCorePoolSize;
    }

    public int getServerMaxPoolSize() {
        return serverMaxPoolSize;
    }

    public void setServerMaxPoolSize(int serverMaxPoolSize) {
        this.serverMaxPoolSize = serverMaxPoolSize;
    }

    public long getServerKeepAliveTime() {
        return serverKeepAliveTime;
    }

    public void setServerKeepAliveTime(long serverKeepAliveTime) {
        this.serverKeepAliveTime = serverKeepAliveTime;
    }

    public int getServerQueueLimit() {
        return serverQueueLimit;
    }

    public void setServerQueueLimit(int serverQueueLimit) {
        this.serverQueueLimit = serverQueueLimit;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getRedoNum() {
        return redoNum;
    }

    public void setRedoNum(int redoNum) {
        this.redoNum = redoNum;
    }

    public int getRedoTimeSplit() {
        return redoTimeSplit;
    }

    public void setRedoTimeSplit(int redoTimeSplit) {
        this.redoTimeSplit = redoTimeSplit;
    }

    public String getGetTimeoutTaskThreadName() {
        return getTimeoutTaskThreadName;
    }

    public void setGetTimeoutTaskThreadName(String getTimeoutTaskThreadName) {
        this.getTimeoutTaskThreadName = getTimeoutTaskThreadName;
    }

    public int getTimeoutLimit() {
        return timeoutLimit;
    }

    public void setTimeoutLimit(int timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    public int getGetTimeoutTaskSleepTime() {
        return getTimeoutTaskSleepTime;
    }

    public void setGetTimeoutTaskSleepTime(int getTimeoutTaskSleepTime) {
        this.getTimeoutTaskSleepTime = getTimeoutTaskSleepTime;
    }

    public String getGetTaskMapperName() {
        return getTaskMapperName;
    }

    public void setGetTaskMapperName(String getTaskMapperName) {
        this.getTaskMapperName = getTaskMapperName;
    }

    public String getGetTimeoutTaskMapperName() {
        return getTimeoutTaskMapperName;
    }

    public void setGetTimeoutTaskMapperName(String getTimeoutTaskMapperName) {
        this.getTimeoutTaskMapperName = getTimeoutTaskMapperName;
    }

    public String getUpdTaskMapperName() {
        return updTaskMapperName;
    }

    public void setUpdTaskMapperName(String updTaskMapperName) {
        this.updTaskMapperName = updTaskMapperName;
    }
}
