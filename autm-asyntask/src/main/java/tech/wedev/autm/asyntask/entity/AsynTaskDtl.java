package tech.wedev.autm.asyntask.entity;

import org.apache.ibatis.type.Alias;

/**
 * 表cosp_asyn_task_dtl
 */
@Alias("AsynTaskDtl")
public class AsynTaskDtl {
    private String taskId;
    private String keywords;
    private String taskType;
    private String subTaskType;
    private String state;
    private int priority;
    private String applyTime;
    private String inQueueTime;
    private String ip;
    private String threadId;
    private String startWorkTime;
    private String endWorkTime;
    private String errCode;
    private String errMsg;
    private int dealNum;
    private int timeoutLimit;
    private String planTime;
    private String refCol1;
    private String refCol2;
    private String refCol3;
    private String info1;
    private String info2;
    private String info3;

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
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

    public int getDealNum() {
        return dealNum;
    }

    public void setDealNum(int dealNum) {
        this.dealNum = dealNum;
    }

    public int getTimeoutLimit() {
        return timeoutLimit;
    }

    public void setTimeoutLimit(int timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    public String getPlanTime() {
        return planTime;
    }

    public void setPlanTime(String planTime) {
        this.planTime = planTime;
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
}
