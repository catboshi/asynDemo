package tech.wedev.autm.asyntask.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wedev.autm.asyntask.AsynTaskGetTaskThread;
import tech.wedev.autm.asyntask.AsynTaskGetTimeoutThread;
import tech.wedev.autm.asyntask.AsynTaskInfo;
import tech.wedev.autm.asyntask.AsynTaskThreadPool;
import tech.wedev.autm.asyntask.entity.AsynTaskConf;
import tech.wedev.autm.asyntask.entity.AsynTaskDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异步任务服务初始化和停止
 */
public class AsynTaskModule {

    private static Logger logger = LoggerFactory.getLogger(AsynTaskModule.class);
    public static AsynTaskModule service = new AsynTaskModule();

    public static AsynTaskModule getInstance() {
        return service;
    }

    private boolean inited = false;

    protected Map<String, AsynTaskInfo> taskDefs;
    protected Map<String, AsynTaskThreadPool> taskPools;
    protected Map<String, AsynTaskGetTaskThread> getTaskThreads;
    protected Map<String, AsynTaskGetTimeoutThread> getTimeoutTaskThreads;

    protected List<AsynTaskConf> subTaskDefs; //子任务列表

    private AsynTaskMonitor monitor;//监控模块

    private AsynTaskModule() {
    }

    /**
     * 是否完成初始化
     *
     * @return 是否完成初始化
     */
    public boolean isInited() {
        return inited;
    }

    public void initialize() throws Throwable {
        logger.info("asyn task begin to initialize");

        //获取子任务配置
        subTaskDefs = getTaskConfList();
        logger.debug("======sub task load successful======");
        logger.debug(subTaskDefs.toString());

        //获取数据库中的任务配置列表
        List<AsynTaskInfo> taskDefList = getTaskInfoList();
        taskDefs = new HashMap<String, AsynTaskInfo>();
        taskPools = new HashMap<String, AsynTaskThreadPool>();
        getTaskThreads = new HashMap<String, AsynTaskGetTaskThread>();
        getTimeoutTaskThreads = new HashMap<String, AsynTaskGetTimeoutThread>();

        if (taskDefList == null || taskDefList.size() == 0) {
            logger.debug("======no task def found, init end======");
            return;
        }

        //启动任务配置，线程、超时线程和线程池
        for (AsynTaskInfo taskDef : taskDefList) {
            //打印任务信息
            String taskType = taskDef.getTaskType();

            logger.debug("======begin to initialize task [" + taskType + "]======");
            logger.debug("task_impl = [" + taskDef.getTaskImpl() + "]");
            logger.debug("task_name = [" + taskDef.getTaskName() + "]");
            logger.debug("fetch_task_interval = [" + taskDef.getGetTaskSleepTime() + "]");
            logger.debug("fetch_task_count = [" + taskDef.getGetTaskNum() + "]");
            logger.debug("pool_size = [" + taskDef.getServerCorePoolSize() + "]");
            logger.debug("queue_limit = [" + taskDef.getServerQueueLimit() + "]");
            logger.debug("redo_count = [" + taskDef.getRedoNum() + "]");
            logger.debug("redo_interval = [" + taskDef.getRedoTimeSplit() + "]");
            logger.debug("timeout_limit = [" + taskDef.getTimeoutLimit() + "]");
            logger.debug("timeout_check_interval = [" + taskDef.getGetTimeoutTaskSleepTime() + "]");

            //添加任务定义到Map
            taskDefs.put(taskType, taskDef);

            //启动任务处理线程池
            logger.debug("task [" + taskType + "] begin to initialize threadpool");

            AsynTaskThreadPool threadPool = new AsynTaskThreadPool();
            threadPool.setAsynTaskInfo(taskDef);
            threadPool.initialize();

            logger.debug("task [" + taskType + "] threadpool initialize successful");

            //添加到任务处理线程池Map
            taskPools.put(taskType, threadPool);

            //启动获取任务线程
            logger.debug("task [" + taskType + "] begin to initialize task get thread");

            AsynTaskGetTaskThread taskGetTaskThread = new AsynTaskGetTaskThread();
            taskGetTaskThread.setAsynTaskInfo(taskDef);
            taskGetTaskThread.setAsynTaskServer(threadPool);
            taskGetTaskThread.initialize();

            logger.debug("task [" + taskType + "] task get thread initialize successful");

            //添加获取任务线程到Map
            getTaskThreads.put(taskType, taskGetTaskThread);

            //启动任务超时监控线程
            if (taskDef.getTimeoutLimit() > 0) {//设置超时监控时间间隔的任务才启动超时线程
                logger.debug("task [" + taskType + "] begin to initialize timeout task get thread");

                AsynTaskGetTimeoutThread timeoutTaskGetThread = new AsynTaskGetTimeoutThread();
                timeoutTaskGetThread.setAsynTaskInfo(taskDef);
                timeoutTaskGetThread.setAsynTaskServer(threadPool);
                timeoutTaskGetThread.initialize();

                logger.debug("task [" + taskType + "] timeout task get thread initialize successful");

                //添加任务超时监控线程到Map
                getTimeoutTaskThreads.put(taskType, timeoutTaskGetThread);
            }

            logger.debug("======end initialize task [" + taskType + "]======");
        }

        //重置状态
        inited = true;
        logger.info("asyn task initialize successful");

    }

    public void terminate() throws Exception {
        logger.info("asyn task begin to terminate");

        if (taskPools != null) {
            for (Map.Entry<String, AsynTaskThreadPool> taskPool : taskPools.entrySet()) {
                try {
                    taskPool.getValue().terminate();
                } catch (Exception e) {
                    logger.error("Thread pool of task [" + taskPool.getKey() + "] terminate exception", e);
                }
            }
        }

        if (getTaskThreads != null) {
            for (Map.Entry<String, AsynTaskGetTaskThread> thread : getTaskThreads.entrySet()) {
                try {
                    thread.getValue().terminate();
                } catch (Exception e) {
                    logger.error("Get task thread of task [" + thread.getKey() + "] terminate exception", e);
                }
            }
        }

        if (getTimeoutTaskThreads != null) {
            for (Map.Entry<String, AsynTaskGetTimeoutThread> thread : getTimeoutTaskThreads.entrySet()) {
                try {
                    thread.getValue().terminate();
                } catch (Exception e) {
                    logger.error("Get task thread of task [" + thread.getKey() + "] terminate exception", e);
                }
            }
        }

        taskDefs = null;
        taskPools = null;
        getTaskThreads = null;
        getTimeoutTaskThreads = null;

        inited = false;
        logger.info("asyn task terminate successful");
    }

    /**
     * 获取任务的定义列表
     */
    public static List<AsynTaskInfo> getTaskInfoList() throws Throwable {

        List<AsynTaskDef> taskDefs = (List<AsynTaskDef>) AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDefService().queryAll();
        if (taskDefs == null) {
            return null;
        }

        List<AsynTaskInfo> taskInfos = new ArrayList<AsynTaskInfo>();
        for (AsynTaskDef taskDef : taskDefs) {
            //数据库配置的基本信息
            AsynTaskInfo taskInfo = new AsynTaskInfo();
            taskInfo.setTaskType(taskDef.getTaskType());
            taskInfo.setTaskName(taskDef.getTaskName());
            taskInfo.setTaskImpl(taskDef.getTaskImpl());
            taskInfo.setGetTaskSleepTime(taskDef.getFetchTaskInterval());
            taskInfo.setGetTaskNum(taskDef.getFetchTaskCount());
            taskInfo.setServerCorePoolSize(taskDef.getMinPoolSize());
            taskInfo.setServerMaxPoolSize(taskDef.getMaxPoolSize());
            taskInfo.setServerQueueLimit(taskDef.getQueueLimit());
            taskInfo.setRedoNum(taskDef.getRedoCount());
            taskInfo.setRedoTimeSplit(taskDef.getRedoInterval());
            taskInfo.setTimeoutLimit(taskDef.getTimeoutLimit());
            taskInfo.setGetTimeoutTaskSleepTime(taskDef.getTimeoutCheckInterval());
            taskInfo.setExecuteTaskDelay(taskDef.getExecuteTaskDelay());

            //额外的固定信息：线程名，mybatis参数等
            String taskType = taskDef.getTaskType();
            taskInfo.setGetTaskType(taskType);
            taskInfo.setGetTaskThreadName(taskType + "_GetTaskThread");
            taskInfo.setServerName(taskType + "_Server");
            taskInfo.setGetTimeoutTaskThreadName(taskType + "_GetTimeoutTaskThread");
            taskInfo.setGetTaskMapperName("");
            taskInfo.setGetTimeoutTaskMapperName("");
            taskInfo.setUpdTaskMapperName("");

            taskInfos.add(taskInfo);
        }

        return taskInfos;
    }

    public static List<AsynTaskConf> getTaskConfList() throws Throwable {
        return (List<AsynTaskConf>) AbstractAsynTaskServiceFactory.getFactory().getAsynTaskConfService().queryAll();
    }

    public AsynTaskMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(AsynTaskMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * 获取对应的任务类型、子类对应的子类配置对象
     *
     * @param taskType    任务类型
     * @param subTaskType 子任务类型
     * @return 子任务配置对象
     */
    public AsynTaskConf getTaskConf(String taskType, String subTaskType) {
        List<AsynTaskConf> localSubTaskConfs = subTaskDefs;
        for (AsynTaskConf subTaskConf :
                localSubTaskConfs) {
            if (subTaskConf.getTaskType().equals(taskType) && subTaskConf.getSubTaskType().equals(subTaskType)) {
                return subTaskConf;
            }
        }
        return null;
    }
}

