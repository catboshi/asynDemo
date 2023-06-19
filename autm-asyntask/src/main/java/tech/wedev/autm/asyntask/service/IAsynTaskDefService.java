package tech.wedev.autm.asyntask.service;

import tech.wedev.autm.asyntask.entity.AsynTaskDef;

import java.util.List;

/**
 * 表cosp_asyn_task_def的增删改查
 */
public interface IAsynTaskDefService {
    /**
     * 获取所有的任务定义
     * @return 任务定义
     */
    public List<AsynTaskDef> queryAll();
}
