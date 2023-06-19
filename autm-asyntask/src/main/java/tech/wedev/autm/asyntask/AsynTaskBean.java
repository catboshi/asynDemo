package tech.wedev.autm.asyntask;

import tech.wedev.autm.asyntask.api.AbstractAsynTaskServiceFactory;
import tech.wedev.autm.asyntask.entity.AsynTaskDtl;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskPriorityType;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskStateType;

import java.math.BigDecimal;

/**
 * 异步任务bean
 */
public class AsynTaskBean {
    private String taskId;//任务编号
    private String keywords;//任务关键字
    private String taskType;//任务类型
    private String subTaskType;//子任务类型
    private String state;// 0待处理 1进入队列 2开始工作 3工作结束
    private TaskPriorityType priority;//优先级
    private String applyTime;//提交时间
    private String inQueueTime;//进入队列时间
    private String ip;//抢占ip
    private String threadId;//工作线程编号
    private String startWorkTime;//开始工作时间
    private String endWorkTime;//结束工作时间
    private String errCode;//错误代码
    private String errMsg;//错误信息
    private BigDecimal dealNum;//已处理次数
    private BigDecimal timeoutLimit;//超时时间限制
    private String planTime;//计划执行时间 yyyy-MM-dd HH24:mi:ss格式
    private BigDecimal planTimeSeconds;//自动转换为多少秒后执行
    private String refCol1;//关联字段1
    private String refCol2;//关联字段2
    private String refCol3;//关联字段3
    private String info1;//备注1
    private String info2;//备注2
    private String info3;//备注3

    /**
     * 各自实现自己干活细节，子类必须继承此方法，不然异步任务就会啥事不干，此处由于超时机制，需要new出来
     * 对象，所以没有用抽象方法
     *
     * @return
     */
    public AsynTaskErrInfo executeEvent() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsynTaskErrInfo();
    }

    ;

    /**
     * 可以重载此方法来进行细致的重做
     *
     * @param errInfo
     * @param taskInfo
     * @return 是否进行重做
     * @throws Throwable
     */
    public boolean addRedoTask(AsynTaskErrInfo errInfo, AsynTaskInfo taskInfo) throws Throwable {
        boolean redoFlag = isNeedRedo(errInfo, taskInfo);
        if (redoFlag) {
            //如果有重做间隔设置，就设置下次执行计划时间；如果没有就为空，立即执行
            int redoTimeSplit = taskInfo.getRedoTimeSplit();
            this.planTime = null;
            if (redoTimeSplit > 0) {
                this.planTimeSeconds = new BigDecimal(redoTimeSplit);
            } else {
                this.planTimeSeconds = null;
            }
            this.dealNum = dealNum.add(new BigDecimal(1));
            this.state = TaskStateType.PEDDING.getCode();
            this.ip = null;
            this.inQueueTime = null;
            AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().insertAsynTask(this);
        }
        return redoFlag;
    }

    public boolean isNeedRedo(AsynTaskErrInfo errInfo, AsynTaskInfo taskInfo) {
        boolean redoFlag = false;
        //如果是失败的并且需要重做，并且处理重做次数小于限定值就重做
        if (!errInfo.isSuccFlag() && errInfo.isRedoFlag()) {
            redoFlag = isNeedRedo(taskInfo);
        }
        return redoFlag;
    }

    public boolean isNeedRedo(AsynTaskInfo taskInfo) {
        if (this.dealNum.intValue() < taskInfo.getRedoNum()) {
            return true;
        }
        return false;
    }

    public static AsynTaskBean getTaskBean(AsynTaskDtl dtl) {
        AsynTaskBean bean = new AsynTaskBean();
        bean.setTaskId(dtl.getTaskId());
        bean.setTaskType(dtl.getTaskType());
        bean.setKeywords(dtl.getKeywords());
        bean.setState(dtl.getState());
        bean.setPriority(TaskPriorityType.getEnum(new BigDecimal(dtl.getPriority())));
        bean.setApplyTime(dtl.getApplyTime());
        bean.setInQueueTime(dtl.getInQueueTime());
        bean.setIp(dtl.getIp());
        bean.setThreadId(dtl.getThreadId());
        bean.setStartWorkTime(dtl.getStartWorkTime());
        bean.setEndWorkTime(dtl.getEndWorkTime());
        bean.setErrCode(dtl.getErrCode());
        bean.setErrMsg(dtl.getErrMsg());
        bean.setDealNum(new BigDecimal(dtl.getDealNum()));
        bean.setTimeoutLimit(new BigDecimal(dtl.getTimeoutLimit()));
        bean.setPlanTime(dtl.getPlanTime());
        bean.setRefCol1(dtl.getRefCol1());
        bean.setRefCol2(dtl.getRefCol2());
        bean.setRefCol3(dtl.getRefCol3());
        bean.setInfo1(dtl.getInfo1());
        bean.setInfo2(dtl.getInfo2());
        bean.setInfo3(dtl.getInfo3());

        return bean;
    }

    public static AsynTaskDtl getTaskDtl(AsynTaskBean bean) {
        AsynTaskDtl dtl = new AsynTaskDtl();
        dtl.setTaskId(bean.taskId);
        dtl.setTaskType(bean.getTaskType());
        dtl.setKeywords(bean.getKeywords());
        dtl.setState(bean.getState());
        dtl.setPriority(bean.getPriority().getValue().intValue());
        dtl.setApplyTime(bean.getApplyTime());
        dtl.setInQueueTime(bean.getInQueueTime());
        dtl.setIp(bean.getIp());
        dtl.setThreadId(bean.getThreadId());
        dtl.setStartWorkTime(bean.getStartWorkTime());
        dtl.setEndWorkTime(bean.getEndWorkTime());
        dtl.setErrCode(bean.getErrCode());
        dtl.setErrMsg(bean.getErrMsg());
        dtl.setDealNum(bean.getDealNum().intValue());
        dtl.setTimeoutLimit(bean.getTimeoutLimit().intValue());
        dtl.setPlanTime(bean.getPlanTime());
        dtl.setRefCol1(bean.getRefCol1());
        dtl.setRefCol2(bean.getRefCol2());
        dtl.setRefCol3(bean.getRefCol3());
        dtl.setInfo1(bean.getInfo1());
        dtl.setInfo2(bean.getInfo2());
        dtl.setInfo3(bean.getInfo3());

        return dtl;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getSubTaskType() {
        return subTaskType;
    }

    public void setSubTaskType(String subTaskType) {
        this.subTaskType = subTaskType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TaskPriorityType getPriority() {
        return priority;
    }

    public void setPriority(TaskPriorityType priority) {
        this.priority = priority;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getInQueueTime() {
        return inQueueTime;
    }

    public void setInQueueTime(String inQueueTime) {
        this.inQueueTime = inQueueTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getStartWorkTime() {
        return startWorkTime;
    }

    public void setStartWorkTime(String startWorkTime) {
        this.startWorkTime = startWorkTime;
    }

    public String getEndWorkTime() {
        return endWorkTime;
    }

    public void setEndWorkTime(String endWorkTime) {
        this.endWorkTime = endWorkTime;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public BigDecimal getDealNum() {
        return dealNum;
    }

    public void setDealNum(BigDecimal dealNum) {
        this.dealNum = dealNum;
    }

    public BigDecimal getTimeoutLimit() {
        return timeoutLimit;
    }

    public void setTimeoutLimit(BigDecimal timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    public String getPlanTime() {
        return planTime;
    }

    public void setPlanTime(String planTime) {
        this.planTime = planTime;
    }

    public BigDecimal getPlanTimeSeconds() {
        return planTimeSeconds;
    }

    public void setPlanTimeSeconds(BigDecimal planTimeSeconds) {
        this.planTimeSeconds = planTimeSeconds;
    }

    public String getRefCol1() {
        return refCol1;
    }

    public void setRefCol1(String refCol1) {
        this.refCol1 = refCol1;
    }

    public String getRefCol2() {
        return refCol2;
    }

    public void setRefCol2(String refCol2) {
        this.refCol2 = refCol2;
    }

    public String getRefCol3() {
        return refCol3;
    }

    public void setRefCol3(String refCol3) {
        this.refCol3 = refCol3;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
    }

    public String getInfo3() {
        return info3;
    }

    public void setInfo3(String info3) {
        this.info3 = info3;
    }

    @Override
    public String toString() {
        return "AsynTaskBean{" +
                "taskId='" + taskId + '\'' +
                ", keywords='" + keywords + '\'' +
                ", taskType='" + taskType + '\'' +
                ", subTaskType='" + subTaskType + '\'' +
                ", state='" + state + '\'' +
                ", priority=" + priority +
                ", applyTime='" + applyTime + '\'' +
                ", inQueueTime='" + inQueueTime + '\'' +
                ", ip='" + ip + '\'' +
                ", threadId='" + threadId + '\'' +
                ", startWorkTime='" + startWorkTime + '\'' +
                ", endWorkTime='" + endWorkTime + '\'' +
                ", errCode='" + errCode + '\'' +
                ", errMsg='" + errMsg + '\'' +
                ", dealNum=" + dealNum +
                ", timeoutLimit=" + timeoutLimit +
                ", planTime='" + planTime + '\'' +
                ", planTimeSeconds=" + planTimeSeconds +
                ", refCol1='" + refCol1 + '\'' +
                ", refCol2='" + refCol2 + '\'' +
                ", refCol3='" + refCol3 + '\'' +
                ", info1='" + info1 + '\'' +
                ", info2='" + info2 + '\'' +
                ", info3='" + info3 + '\'' +
                '}';
    }
}
