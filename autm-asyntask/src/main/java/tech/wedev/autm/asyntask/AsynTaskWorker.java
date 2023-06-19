package tech.wedev.autm.asyntask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wedev.autm.asyntask.api.AbstractAsynTaskServiceFactory;
import tech.wedev.autm.asyntask.api.AsynTaskModule;
import tech.wedev.autm.asyntask.api.IAsynTask;
import tech.wedev.autm.asyntask.entity.AsynTaskConf;

/**
 * 异步任务工作线程类
 */
public class AsynTaskWorker implements Runnable, Comparable<AsynTaskWorker> {

    private int priority;//优先级
    private AsynTaskBean taskBean;//任务bean
    private AsynTaskInfo asynTaskInfo;//异步任务信息
    private AsynTaskThreadPool asynTaskServer;//异步任务线程池信息
    private Logger log;

    public AsynTaskWorker(AsynTaskBean taskBean, AsynTaskInfo asynTaskInfo, AsynTaskThreadPool asynTaskServer) {
        this.priority = taskBean.getPriority().getValue().intValue();
        this.taskBean = taskBean;
        this.asynTaskInfo = asynTaskInfo;
        this.asynTaskServer = asynTaskServer;
        log = LoggerFactory.getLogger(AsynTaskWorker.class);//logger修改为自己获取
    }

    @Override
    public int compareTo(AsynTaskWorker o) {
        if (priority > o.priority) {
            return -1;
        } else {
            if (priority < o.priority) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    public int getPriority() {
        return priority;
    }

    public AsynTaskBean getTaskBean() {
        return taskBean;
    }

    public void setTaskBean(AsynTaskBean taskBean) {
        this.taskBean = taskBean;
    }

    public AsynTaskInfo getAsynTaskInfo() {
        return asynTaskInfo;
    }

    public void setAsynTaskInfo(AsynTaskInfo asynTaskInfo) {
        this.asynTaskInfo = asynTaskInfo;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    @Override
    public void run() {
        log.debug("开始处理任务: " + taskBean);

        //开始工作后移除队列map中的数据
        asynTaskServer.removeMap(taskBean.getTaskId());
        //先把当前任务状态改为开始处理
        String threadId = Thread.currentThread().getName();
        asynTaskInfo.addThreadMap(threadId, Thread.currentThread());
        try {
            taskBean.setThreadId(threadId);
            log.debug("更新任务状态为开始工作");
            AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().updateStateToStart(taskBean);
            log.debug("更新任务状态为开始工作成功");
        } catch (Throwable e) {
            log.error(asynTaskInfo.getServerName() + "更新任务状态为开始工作异常: "
                    + taskBean, e);
            return;
        }

        //开始干自己的事
        AsynTaskErrInfo errInfo;
        String clazz = null;
        try {
            //修改为任务表中定义的接口
            //实现类的获取，修改为优先从子任务配置获取，如果未定义则从父任务获取
            AsynTaskConf conf = (AsynTaskConf) AsynTaskModule.getInstance().getTaskConf(taskBean.getTaskType(), taskBean.getSubTaskType());
            if (conf != null) {
                clazz = conf.getSubTaskImpl();
            } else {
                clazz = asynTaskInfo.getTaskImpl();
            }
            log.debug("开始调用配置的处理类[" + clazz + "]进行处理");
            IAsynTask task = (IAsynTask) AbstractAsynTaskServiceFactory.getFactory().getIAsynTask(clazz);
            errInfo = task.executeTask(taskBean);
            log.debug("处理类调用结束");
        } catch (Exception e) {
            String msg = asynTaskInfo.getServerName() + "线程[" + threadId
                    + "]调用处理类[" + clazz + "]异常: " + e.getMessage();
            log.error(msg, e);
            //如果由框架捕获到异常这里设置为不重做，原因：超时场景，线程中断可能会被这里捕获，超时场景会自动发起重做
            errInfo = new AsynTaskErrInfo(false, false, "C10100001", msg, e);
        }

        //结束时，把任务状态改为处理成功/失败
        try {
            if (errInfo.isSuccFlag()) {
                log.debug("处理类返回成功，开始更新任务状态为成功");
                AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().updateStateToSuccess(taskBean);
                log.debug("更新任务状态成功：taskId=" + taskBean.getTaskId());
            } else {
                log.error("处理类返回失败: " + errInfo.getErrCode() + " "
                        + errInfo.getErrMsg(), errInfo.getThrowable());
                taskBean.setErrCode(errInfo.getErrCode());
                taskBean.setErrMsg(errInfo.getErrMsg());
                log.debug("开始更新任务状态为失败");

                AbstractAsynTaskServiceFactory.getFactory()
                        .getAsynTaskDtlService().updateStateToFail(taskBean);
                log.debug("更新任务状态成功");
            }
            //将记录清理到历史表
            //将成功/失败记录清理到历史表
            try {
                int insert = (Integer) AbstractAsynTaskServiceFactory.getFactory()
                        .getAsynTaskDtlService().insertTaskToHis(taskBean.getTaskId());
                if (insert == 1) {
                    log.debug("将任务插入历史表成功：taskId=" + taskBean.getTaskId());
                    AbstractAsynTaskServiceFactory.getFactory()
                            .getAsynTaskDtlService().deleteTask(taskBean.getTaskId());
                    log.debug("将任务删除成功：taskId=" + taskBean.getTaskId());
                }
            } catch (Throwable e) {
                log.error(asynTaskInfo.getServerName() + "将处理成功的任务移入历史表异常：taskId="
                        + taskBean.getTaskId(), e);
            }
        } catch (Throwable e) {
            log.error(asynTaskInfo.getServerName() + "修改状态为处理成功/失败异常："
                    + taskBean, e);
            return;
        }

        //如果需要重做，内部会自己判断
        try {
            log.debug("将任务加入重做队列: " + taskBean);
            taskBean.addRedoTask(errInfo, asynTaskInfo);
            log.debug("将任务加入重做结束");
        } catch (Throwable e) {
            log.error(asynTaskInfo.getServerName() + "将任务加入重做队列异常: " + taskBean,
                    e);
        }
    }
}
