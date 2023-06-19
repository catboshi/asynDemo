package tech.wedev.autm.asyntask.entity;

import org.apache.ibatis.type.Alias;

/**
 * 表cosp_asyn_task_def
 */
@Alias("AsynTaskDef")
public class AsynTaskDef {
    private String taskType;
    private String taskName;
    private String taskImpl;
    private int fetchTaskInterval;
    private int fetchTaskCount;
    private int executeTaskDelay;
    private int minPoolSize;
    private int maxPoolSize;
    private int queueLimit;
    private int redoCount;
    private int redoInterval;
    private int timeoutLimit;
    private int timeoutCheckInterval;

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

    public String getTaskImpl() {
        return taskImpl;
    }

    public void setTaskImpl(String taskImpl) {
        this.taskImpl = taskImpl;
    }

    public int getFetchTaskInterval() {
        return fetchTaskInterval;
    }

    public void setFetchTaskInterval(int fetchTaskInterval) {
        this.fetchTaskInterval = fetchTaskInterval;
    }

    public int getFetchTaskCount() {
        return fetchTaskCount;
    }

    public void setFetchTaskCount(int fetchTaskCount) {
        this.fetchTaskCount = fetchTaskCount;
    }

    public int getExecuteTaskDelay() {
        return executeTaskDelay;
    }

    public void setExecuteTaskDelay(int executeTaskDelay) {
        this.executeTaskDelay = executeTaskDelay;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueLimit() {
        return queueLimit;
    }

    public void setQueueLimit(int queueLimit) {
        this.queueLimit = queueLimit;
    }

    public int getRedoCount() {
        return redoCount;
    }

    public void setRedoCount(int redoCount) {
        this.redoCount = redoCount;
    }

    public int getRedoInterval() {
        return redoInterval;
    }

    public void setRedoInterval(int redoInterval) {
        this.redoInterval = redoInterval;
    }

    public int getTimeoutLimit() {
        return timeoutLimit;
    }

    public void setTimeoutLimit(int timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    public int getTimeoutCheckInterval() {
        return timeoutCheckInterval;
    }

    public void setTimeoutCheckInterval(int timeoutCheckInterval) {
        this.timeoutCheckInterval = timeoutCheckInterval;
    }
}
