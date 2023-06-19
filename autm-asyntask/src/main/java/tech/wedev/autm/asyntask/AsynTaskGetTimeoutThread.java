package tech.wedev.autm.asyntask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wedev.autm.asyntask.api.AbstractAsynTaskServiceFactory;
import tech.wedev.autm.asyntask.api.AsynTaskModule;
import tech.wedev.autm.asyntask.api.AsynTaskMonitor;
import tech.wedev.autm.asyntask.utils.SystemUtil;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskStateType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异步任务获取超时任务线程
 */
public class AsynTaskGetTimeoutThread extends AbstractAsynTask implements Runnable {

    private boolean running = true;
    private Logger log;
    private Thread getTimeoutTaskThread = null;
    private AsynTaskThreadPool asynTaskServer;
    private AsynTaskInfo asynTaskInfo;
    private ThreadStatus status;

    /**
     * 超时策略，如果超时一倍时间，只查找属于自己IP的任务，杀线程，然后任务重做；如果超过两倍时间，查IP
     * 上的任务，如果IP和自己一致就杀线程，然后任务重做
     */
    @Override
    public void run() {
        try {
            log.debug("线程启动后开始休眠60秒");
            Thread.sleep(60000);//线程启动后，先休息1分钟再干活，避免因为其他东西未加载而导致的问题
            log.debug("线程启动休眠60秒结束");
        } catch (InterruptedException e) {
            log.error("任务类型" + asynTaskInfo.getGetTaskType() + "获取超时任务线程"
                    + asynTaskInfo.getGetTimeoutTaskThreadName() + "启动后休眠60秒异常", e);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ip", SystemUtil.getContainerIdOrHostIp());
        map.put("taskType", asynTaskInfo.getGetTaskType());
        AsynTaskErrInfo errInfo = new AsynTaskErrInfo(false, true, "C10100002", "异常任务超时", null);
        int usabilityCount = 0;
        try {
            while (running) {
                try {
                    //Report status
                    status.setLastReportTime(new Date());

                    //增加监控接口，由应用自行报送
                    AsynTaskMonitor usabilityMonitor = AsynTaskModule.getInstance().getMonitor();
                    if (usabilityMonitor != null) {
                        try {
                            boolean flag = usabilityMonitor.taskTimeOutUsabilityMonitor(asynTaskInfo.getTaskType(),
                                    asynTaskInfo.getGetTimeoutTaskSleepTime(), usabilityCount);
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


                    log.debug("开始抢占超时任务");
                    //先对大于两倍超时时间的任务进行抢夺，抢完状态变6超时被抢 IP变为本机
                    int updTaskCount = (Integer) AbstractAsynTaskServiceFactory.getFactory()
                            .getAsynTaskDtlService().updateStateAndIpWhenDoubleTimeout((String) map.get("ip"), (String) map.get("taskType"));

                    log.debug("抢占超时任务记录数=[" + updTaskCount + "]");

                    //增加抢占任务数大于0的判断
                    if (updTaskCount > 0) {

                        log.debug("开始获取已抢占超时任务");

                        //再查找超时时间的属于本IP的任务进行查杀及重做
                        List<AsynTaskBean> taskList = (List<AsynTaskBean>) AbstractAsynTaskServiceFactory.getFactory()
                                .getAsynTaskDtlService().queryMyIpTimeoutTaskList((String) map.get("ip"), (String) map.get("taskType"));

                        log.debug("获取已抢占超时任务记录=[" + taskList + "]");

                        for (AsynTaskBean taskBean : taskList) {
                            //状态为工作中的任务，需要进行杀线程动作
                            if (TaskStateType.getEnum(taskBean.getState()) == TaskStateType.START_WORK
                                    && SystemUtil.getContainerIdOrHostIp().equals(taskBean.getIp())) {
                                Thread worker = asynTaskInfo.getThread(taskBean.getThreadId());

                                if (worker != null) {
                                    try {
                                        log.debug("任务正在执行，尝试杀任务: " + taskBean);
                                        worker.interrupt();
                                        log.debug("尝试杀任务成功");
                                    } catch (Exception e) {
                                        log.warn(asynTaskInfo.getGetTimeoutTaskThreadName() + "杀线程[" + worker.getId()
                                                + "]异常: " + taskBean, e);
                                    }
                                }
                            }
                            //状态为进入队列的任务，需要移除队列
                            if (TaskStateType.getEnum(taskBean.getState()) == TaskStateType.IN_QUEUE
                                    && SystemUtil.getContainerIdOrHostIp().equals(taskBean.getIp())) {
                                log.debug("尝试从队列中移除任务: " + taskBean);
                                //如果从队列里面移除任务失败，直接continue，不做任何处理（当出现该场景，说明正巧这时候任务在执行）
                                boolean result = !asynTaskServer.removeQueueAndMap(taskBean.getTaskId());
                                if (result) {
                                    log.debug("从队列中移除任务成功");
                                    continue;
                                } else {
                                    log.debug("从队列中移除任务失败");
                                }
                            }

                            taskBean.setErrCode("C10100002");
                            taskBean.setErrMsg("异常任务超时");
                            //杀掉后，把任务状态改为超时被杀
                            try {
                                log.debug("将任务状态更新为超时被杀: " + taskBean);
                                AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().updateStateToKill(taskBean);
                                log.debug("将任务状态更新为超时被杀成功");
                            } catch (Throwable throwable) {
                                log.error(asynTaskInfo.getGetTimeoutTaskThreadName() + "将任务状态更新为超时被杀异常" + taskBean, throwable);
                                continue;
                            }

                            //如果需要重做，内部会自己判断
                            try {
                                log.debug("将任务加入重做队列: " + taskBean);
                                taskBean.addRedoTask(errInfo, asynTaskInfo);
                                log.debug("将任务加入重做结束: " + taskBean);
                            } catch (Throwable throwable) {
                                log.error(asynTaskInfo.getGetTimeoutTaskThreadName() + "添加重做任务异常", throwable);
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    log.error(asynTaskInfo.getGetTimeoutTaskThreadName()
                            + "处理超时任务过程中出现异常", throwable);
                }

                log.debug("单次扫描待处理超时任务结束，等待下一次扫描");
                Thread.sleep(asynTaskInfo.getGetTimeoutTaskSleepTime());
            }
        } catch (InterruptedException e) {
            log.error(asynTaskInfo.getGetTimeoutTaskThreadName() + "超时处理线程被中断",
                    e);
        }
    }

    /**
     * 销毁执行方法
     */
    public void terminate() {
        this.running = false;
        try {
            if (this.getTimeoutTaskThread != null) {
                this.getTimeoutTaskThread.interrupt();
            }
            log.info(asynTaskInfo.getGetTimeoutTaskThreadName() + "超时处理线程销毁成功"
                    + getTimeoutTaskThread);
        } catch (Exception e) {
            log.error(asynTaskInfo.getGetTimeoutTaskThreadName() + "超时处理线程销毁异常", e);
        }
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
            log = LoggerFactory.getLogger(AsynTaskGetTimeoutThread.class);
            this.running = true;
            this.getTimeoutTaskThread = new Thread(this);
            this.getTimeoutTaskThread.setName(asynTaskInfo.getGetTimeoutTaskThreadName());
            this.getTimeoutTaskThread.start();
            status = new ThreadStatus();
            status.setThreadName(asynTaskInfo.getGetTimeoutTaskThreadName());
            status.setTaskType(asynTaskInfo.getGetTaskType());
            status.setLastReportTime(new Date());
            status.setThreadPoolIp(SystemUtil.getContainerIdOrHostIp());
            log.info(asynTaskInfo.getGetTimeoutTaskThreadName() + "超时处理线程启动成功,"
                    + getTimeoutTaskThread);
        } catch (Exception e) {
            log.error(asynTaskInfo.getGetTimeoutTaskThreadName() + "超时处理线程启动异常", e);
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
}
