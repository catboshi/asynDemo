package tech.wedev.autm.asyntask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wedev.autm.asyntask.api.AbstractAsynTaskServiceFactory;
import tech.wedev.autm.asyntask.utils.SystemUtil;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskPriorityType;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskStateType;

import java.math.BigDecimal;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务线程池类
 */
public class AsynTaskThreadPool {
    private PriorityBlockingQueue<Runnable> asynTaskQueue = new PriorityBlockingQueue<Runnable>();
    //整一个类来维护队列
    private AsynTaskMap asynTaskMap = new AsynTaskMap();
    private ThreadPoolExecutor asynTaskServer;
    private AsynTaskInfo asynTaskInfo;
    private Logger log;

    /**
     * 初始化执行方法
     */
    public void initialize() {
        try {
            log = LoggerFactory.getLogger(AsynTaskThreadPool.class);
            asynTaskInfo.clearThreadMap();
            asynTaskMap.clear();
            asynTaskServer = new ThreadPoolExecutor(
                    asynTaskInfo.getServerCorePoolSize(),
                    asynTaskInfo.getServerMaxPoolSize(),
                    asynTaskInfo.getServerKeepAliveTime(), TimeUnit.SECONDS,
                    asynTaskQueue);
            AsynTaskThreadPoolManager.addPool(asynTaskInfo.getGetTaskType(),
                    this);
            log.info("线程池" + asynTaskInfo.getGetTaskType() + "启动成功");
        } catch (IllegalArgumentException e) {
            log.error(asynTaskInfo.getServerName() + "被中断", e);
        }
    }

    /**
     * 销毁执行方法
     */
    public void terminate() {
        if (asynTaskServer!=null) {
            try {
                asynTaskServer.shutdownNow();
            } catch (Exception e) {
                log.error(asynTaskInfo.getServerName() + "销毁异常", e);
            }
        }
        synchronized (asynTaskMap) {
            //如果队列不为空，则把队列里面的任务都重置
            if (asynTaskQueue.size()>0) {
                AsynTaskBean taskBean = new AsynTaskBean();
                taskBean.setIp(SystemUtil.getContainerIdOrHostIp());
                taskBean.setTaskType(asynTaskInfo.getGetTaskType());
                try {
                    AbstractAsynTaskServiceFactory.getFactory()
                            .getAsynTaskDtlService().updateMyIpTaskStateToPending(taskBean);
                } catch (Throwable e) {
                    log.error(asynTaskInfo.getServerName() + "队列里的任务重置失败", e);
                }
                asynTaskQueue.clear();
                asynTaskMap.clear();
            }
        }
    }

    /**
     * 移除队列，任务超时时调用，仅移除队列，任务状态由调用方修改为超时被杀
     * @param taskId
     * @return
     */
    public boolean removeQueueAndMap(String taskId) {
        if (taskId==null||"".equals(taskId)) {
            return false;
        }
        synchronized (asynTaskMap) {
            AsynTaskWorker taskWorker = asynTaskMap.getWorker(taskId);
            if (taskWorker!=null) {
                asynTaskQueue.remove(taskWorker);
                asynTaskMap.removeWorker(taskWorker);
                log.debug("超时移除队列，任务编号" + taskId);
                return true;
            }
            return false;
        }
    }

    /**
     * 开始工作时调用，移除Map
     * @param taskId
     */
    public void removeMap(String taskId) {
        synchronized (asynTaskMap) {
            log.debug("移除队列，taskId=[" + taskId + "]");
            asynTaskMap.removeWorker(taskId);
        }
    }

    /**
     * 移除优先级最低的task，同时把任务状态修改为待处理
     * @param priority
     * @return
     */
    public boolean removeLowestPriorityWorker(TaskPriorityType priority) {
        synchronized (asynTaskMap) {
            AsynTaskWorker lowerWorker = asynTaskMap.getLowestWorker();
            if (lowerWorker != null && priority.getValue().intValue() > lowerWorker.getPriority()) {
                try {
                    AbstractAsynTaskServiceFactory.getFactory()
                            .getAsynTaskDtlService().updateStateToPending(lowerWorker.getTaskBean());
                } catch (Throwable e) {
                    log.error("队列丽的任务重置失败" + asynTaskInfo.getServerName(), e);
                    return false;
                }
                asynTaskQueue.remove(lowerWorker);
                asynTaskMap.removeWorker(lowerWorker);
                log.debug("移除最低优先级任务，taskId=["
                +lowerWorker.getTaskBean().getTaskId()+"]，任务优先级=["
                +lowerWorker.getPriority() + "]");
                return true;
            }
        }
        return false;
    }

    /**
     * 是否繁忙
     * @return
     */
    public boolean isBusy() {
        if (this.asynTaskQueue.size()>= asynTaskInfo.getServerQueueLimit()) {
            log.debug("现在的队列长度是" + asynTaskQueue.size() + ";状态繁忙");
            return true;
        }
        return false;
    }

    /**
     * 添加发报队列
     * @param worker
     */
    public void execute(AsynTaskWorker worker) {
        synchronized (asynTaskMap) {
            log.debug("加入队列，taskId=[" + worker.getTaskBean().getTaskId() + "]在队列长度["
                    + asynTaskQueue.size() + "]");
            asynTaskMap.addWorker(worker);
        }
        asynTaskServer.execute(worker);
    }

    /**
     * 添加异步任务，任务编号自动生成，必输字段keyword priority refCol1
     * @param taskBean
     * @return
     * @throws Throwable
     */
    public int addAsynTask(AsynTaskBean taskBean) throws Throwable {
        //如果当前池忙碌，插异步任务；如果当前池空闲，插入队列的任务，并把它加入到队列中
        if (taskBean.getPlanTime() == null
                && taskBean.getPlanTimeSeconds() == null) {
            if (!isBusy()) {
                log.debug("添加异步任务，线程池状态不忙碌，直接将任务添加到队列");
                return insertInQueueTask(taskBean);
            } else {
                log.debug("添加异步任务，线程池状态忙碌，插入数据库待处理任务");
                return insertPendingTask(taskBean);
            }
        } else {
            return insertPendingTask(taskBean);
        }
    }

    /**
     * 添加异步任务，不管任务繁忙状态只插异步任务，必输字段keyword priority refCol1
     * @param taskBean
     * @return
     * @throws Throwable
     */
    public int addOnlyAsynTask(AsynTaskBean taskBean) throws Throwable {
        return insertPendingTask(taskBean);
    }

    /**
     * 添加联机任务，任务编号自动生成，必输字段keyword priority refCol1
     * @param taskBean
     * @return
     * @throws Throwable
     */
    public int addOnlineTask(AsynTaskBean taskBean) throws Throwable {
        if (taskBean.getPlanTime()==null
                &&taskBean.getPlanTimeSeconds()==null) {
            taskBean.setPriority(TaskPriorityType.FIRST);//优先级最高
            //如果空闲，插入队列的任务；如果繁忙则进行移除低优先级队列，如果移除成功，加入当前任务；如果移除失败，添加异步任务
            if (!isBusy()) {
                log.debug("添加联机任务，线程池状态不忙碌，直接将任务添加到队列"
                        + taskBean.getTaskId());
                return insertInQueueTask(taskBean);
            }else {
                boolean removeFlag = removeLowestPriorityWorker(TaskPriorityType.FIRST);
                if (removeFlag) {
                    log.debug("添加联机任务，低优先级任务移除成功，直接将任务添加到队列"
                            + taskBean.getTaskId());
                    return insertInQueueTask(taskBean);
                }else {
                    log.debug("添加联机任务，低优先级任务移除失败，插入数据库待处理任务"
                            + taskBean.getTaskId());
                    return insertPendingTask(taskBean);
                }
            }
        }else {
            return insertPendingTask(taskBean);
        }
    }

    /**
     * 插入一条待处理任务
     * @param taskBean
     * @return
     * @throws Throwable
     */
    private int insertPendingTask(AsynTaskBean taskBean) throws Throwable {
        taskBean.setTimeoutLimit(new BigDecimal(asynTaskInfo.getTimeoutLimit()));
        if (taskBean.getDealNum()==null) {
            taskBean.setDealNum(new BigDecimal(1));
        }
        taskBean.setTaskType(asynTaskInfo.getGetTaskType());
        taskBean.setState(TaskStateType.PEDDING.getCode());//待处理

        return (Integer) AbstractAsynTaskServiceFactory.getFactory()
                .getAsynTaskDtlService().insertAsynTask(taskBean);
    }

    /**
     * 插入一条进入队列的任务
     * @param taskBean
     * @return
     * @throws Throwable
     */
    private int insertInQueueTask(AsynTaskBean taskBean) throws Throwable {
        taskBean.setTimeoutLimit(new BigDecimal(asynTaskInfo.getTimeoutLimit()));
        if (taskBean.getDealNum()==null) {
            taskBean.setDealNum(new BigDecimal(1));
        }
        taskBean.setTaskType(asynTaskInfo.getGetTaskType());
        taskBean.setState(TaskStateType.IN_QUEUE.getCode());
        taskBean.setInQueueTime("systimestamp");
        taskBean.setIp(SystemUtil.getContainerIdOrHostIp());
        int ret = (Integer) AbstractAsynTaskServiceFactory.getFactory()
                .getAsynTaskDtlService().insertAsynTask(taskBean);

        //如果没有用到事务，直接插入队列，如果用到事务，添加全局线程变量
        AsynTaskWorker worker = new AsynTaskWorker(taskBean, asynTaskInfo, this);

        this.execute(worker);
        return ret;
    }

    /**
     * 任务撤销，只能撤销待处理的任务
     *
     * @param taskId
     * @param cancelCode
     * @param cancelMsg
     * @return 被撤销的任务数量，不为1就是错误
     */
    public int cancelTask(String taskId, String cancelCode, String cancelMsg) throws Throwable {
        AsynTaskBean taskBean = new AsynTaskBean();
        taskBean.setTaskId(taskId);
        taskBean.setErrCode(cancelCode);
        taskBean.setErrMsg(cancelMsg);

        return (Integer) AbstractAsynTaskServiceFactory.getFactory()
                .getAsynTaskDtlService().updateStateToCancel(taskBean);
    }

    public AsynTaskInfo getAsynTaskInfo() {
        return asynTaskInfo;
    }

    public void setAsynTaskInfo(AsynTaskInfo asynTaskInfo) {
        this.asynTaskInfo = asynTaskInfo;
    }
}
