package tech.wedev.autm.asyntask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wedev.autm.asyntask.api.AbstractAsynTaskServiceFactory;
import tech.wedev.autm.asyntask.api.AsynTaskModule;
import tech.wedev.autm.asyntask.api.AsynTaskMonitor;
import tech.wedev.autm.asyntask.utils.SystemUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异步任务获取任务线程
 */
public class AsynTaskGetTaskThread extends AbstractAsynTask implements Runnable {

    private boolean running = true;
    private Logger log;
    private Thread getTaskThread = null;
    private AsynTaskThreadPool asynTaskServer;
    private AsynTaskInfo asynTaskInfo;
    private ThreadStatus status;

    @Override
    public void run() {
        try {
            log.debug("线程启动后开始休眠60秒");
            Thread.sleep(60000);//线程启动后，先休息1分钟再干活，避免因为其他东西未加载而导致的问题
            log.debug("线程启动休眠60秒结束");
        } catch (InterruptedException e) {
            log.error("任务类型" + asynTaskInfo.getGetTaskType() + "获取任务线程"
                    + asynTaskInfo.getGetTaskThreadName() + "启动后休眠60秒异常", e);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ip", SystemUtil.getContainerIdOrHostIp());
        map.put("getTaskNum", asynTaskInfo.getGetTaskNum());
        map.put("tasktype", asynTaskInfo.getGetTaskType());
        int updTaskCount;
        int continuousBusyCount = 0;//持续忙碌时间
        int usabilityCount = 0;
        boolean ctsFetchFlag;
        try {
            while (running) {
                log.info("线程启动，准备扫描任务");
                ctsFetchFlag = false;
                //Report status
                status.setLastReportTime(new Date());

                //增加监控接口，由应用自行报送
                AsynTaskMonitor usabilityMonitor = AsynTaskModule.getInstance().getMonitor();
                if (usabilityMonitor != null) {
                    try {
                        boolean flag = usabilityMonitor.taskUsabilityMonitor(asynTaskInfo.getTaskType(),
                                asynTaskInfo.getGetTaskSleepTime(), usabilityCount);
                        if (flag) {
                            usabilityCount = 0;
                        } else {
                            usabilityCount++;
                            if (usabilityCount < 0) {
                                usabilityCount = 0;
                            }
                        }
                    } catch (Throwable throwable) {
                        log.error("可用性监控调用出现异常: taskType=" + asynTaskInfo.getTaskType(), throwable);
                    }
                }

                //如果队列没有超过阈值，就去获取任务数据
                //添加busy监控
                if (asynTaskServer.isBusy()) {
                    //status.setLastBusyTime(new Date())
                    continuousBusyCount++;
                    AsynTaskMonitor monitor = AsynTaskModule.getInstance().getMonitor();
                    if (monitor != null) {
                        try {
                            monitor.taskBusyMonitor(asynTaskInfo.getTaskType(),
                                    continuousBusyCount, asynTaskInfo.getGetTaskSleepTime());
                        } catch (Throwable throwable) {
                            log.error("忙碌监控调用出现异常: taskType=" + asynTaskInfo.getTaskType(), throwable);
                        }
                    }
                } else {
                    //status.setLastBusyTime(null)
                    continuousBusyCount = 0;
                    //先去抢任务，同时把IP
                    try {
                        log.debug("开始抢占任务");
                        updTaskCount = asynTaskInfo.robTask(map);
                        log.debug("抢占任务记录数=[" + updTaskCount + "]");
                    } catch (Exception e) {
                        log.error(asynTaskInfo.getGetTaskThreadName()
                                + "抢任务异常，更新条件: " + map, e);
                        updTaskCount = 0;
                    }
                    if (updTaskCount > 0) {
                        ctsFetchFlag = true;
                        try {
                            //先获取任务集合，然后修改状态，修改完后再加入队列
                            log.debug("开始获取已抢占任务");

                            //修改未mybatis方式
                            List<AsynTaskBean> taskList = (List<AsynTaskBean>) AbstractAsynTaskServiceFactory.getFactory()
                                    .getAsynTaskDtlService().queryRobList(SystemUtil.getContainerIdOrHostIp(),
                                            asynTaskInfo.getGetTaskType());

                            log.debug("获取已抢占任务记录=[" + taskList + "]");

                            try {
                                log.debug("开始更新已抢占任务状态为进入队列");

                                //把任务的状态置为进入队列和修改时间
                                int count = AbstractAsynTaskServiceFactory.getFactory()
                                        .getAsynTaskDtlService().updateRobListAfterPutInQueue(
                                                SystemUtil.getContainerIdOrHostIp(),
                                                asynTaskInfo.getGetTaskType());

                                log.debug("更新已抢占任务状态为进入队列记录数=[" + count + "]");
                            } catch (Throwable throwable) {
                                log.error(asynTaskInfo.getGetTaskThreadName()
                                        + "更新已抢占任务状态为进入队列状态异常，更新条件: " + map, throwable);
                            }

                            for (AsynTaskBean task : taskList) {
                                log.debug("将任务加入线程池，准备执行，任务=" + task);
                                AsynTaskWorker dat = new AsynTaskWorker(task,
                                        asynTaskInfo, asynTaskServer);

                                //增加执行延迟
                                int delay = asynTaskInfo.getExecuteTaskDelay();
                                if (delay != 0) {
                                    log.debug("准备任务执行延迟，taskId=[" + task.getTaskId() + "], delay=[" + delay + "]");
                                    Thread.sleep(delay);
                                }
                                asynTaskServer.execute(dat);
                            }
                        } catch (Throwable throwable) {
                            log.error(asynTaskInfo.getGetTaskThreadName()
                                    + "获取任务异常，查询条件: " + map, throwable);
                        }
                    }
                }

                log.debug("单次扫描待处理任务结束，等待下一次扫描");
                if (ctsFetchFlag == false) {
                    Thread.sleep(asynTaskInfo.getGetTaskSleepTime());
                } else {
                    //如果还有数据获取，则随机睡眠1秒以内
                    double random = Math.random() * 1000;
                    Thread.sleep((new Double(random)).longValue());
                }
            }
        } catch (InterruptedException e) {
            log.error(asynTaskInfo.getGetTaskThreadName() + "线程处理被中断", e);
        }
    }

    /**
     * 销毁执行方法
     */
    public void terminate() {
        this.running = false;
        try {
            if (this.getTaskThread != null) {
                this.getTaskThread.interrupt();
            }
            asynTaskInfo.clearThreadMap();
            log.info(asynTaskInfo.getGetTaskThreadName() + "线程销毁成功"
                    + getTaskThread);
        } catch (Exception e) {
            log.error(asynTaskInfo.getGetTaskThreadName() + "线程销毁异常", e);
        }
    }

    public AsynTaskThreadPool getAsynTaskServer() {
        return asynTaskServer;
    }

    public void setAsynTaskServer(AsynTaskThreadPool asynTaskServer) {
        this.asynTaskServer = asynTaskServer;
    }

    public AsynTaskInfo getAsynTaskInfo() {
        return asynTaskInfo;
    }

    public void setAsynTaskInfo(AsynTaskInfo asynTaskInfo) {
        this.asynTaskInfo = asynTaskInfo;
    }

    @Override
    public ThreadStatus getStatus() {
        return this.status;
    }

    /**
     * 初始化执行方法
     */
    public void initialize() {
        try {
            log = LoggerFactory.getLogger(AsynTaskGetTaskThread.class);
            this.running = true;
            this.getTaskThread = new Thread(this);
            this.getTaskThread.setName(asynTaskInfo.getGetTaskThreadName());
            this.getTaskThread.start();
            status = new ThreadStatus();
            status.setThreadName(asynTaskInfo.getGetTaskThreadName());
            status.setTaskType(asynTaskInfo.getGetTaskType());
            status.setLastReportTime(new Date());
            status.setThreadPoolIp(SystemUtil.getContainerIdOrHostIp());
            log.info(asynTaskInfo.getGetTaskThreadName() + "线程启动成功,"
                    + getTaskThread);
        } catch (Exception e) {
            log.error(asynTaskInfo.getGetTaskThreadName() + "线程启动异常", e);
        }
    }

}
