package tech.wedev.autm.asyntask.entity;

import org.apache.ibatis.type.Alias;

/**
 * 表cosp_asyn_task_conf
 */
@Alias("AsynTaskConf")
public class AsynTaskConf {
    private String taskType;
    private String subTaskType;
    private String subTaskName;
    private String subTaskImpl;

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

    public String getSubTaskName() {
        return subTaskName;
    }

    public void setSubTaskName(String subTaskName) {
        this.subTaskName = subTaskName;
    }

    public String getSubTaskImpl() {
        return subTaskImpl;
    }

    public void setSubTaskImpl(String subTaskImpl) {
        this.subTaskImpl = subTaskImpl;
    }

    @Override
    public String toString() {
        return "task_type='" + taskType + '\'' +
                ", sub_task_type='" + subTaskType + '\'' +
                ", sub_task_name='" + subTaskName + '\'' +
                ", sub_task_impl='" + subTaskImpl + '\'';
    }
}
