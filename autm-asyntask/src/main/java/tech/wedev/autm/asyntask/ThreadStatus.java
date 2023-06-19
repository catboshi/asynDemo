package tech.wedev.autm.asyntask;

import java.util.Date;

public class ThreadStatus {
    private String threadName;
    private String taskType;
    private String threadPoolIp;
    private Date lastReportTime;
    private Date lastBusyTime;

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getThreadPoolIp() {
        return threadPoolIp;
    }

    public void setThreadPoolIp(String threadPoolIp) {
        this.threadPoolIp = threadPoolIp;
    }

    public Date getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(Date lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    public Date getLastBusyTime() {
        return lastBusyTime;
    }

    public void setLastBusyTime(Date lastBusyTime) {
        this.lastBusyTime = lastBusyTime;
    }
}
