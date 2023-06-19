package tech.wedev.autm.asyntask.service;

import tech.wedev.autm.asyntask.entity.AsynTaskConf;

import java.util.List;

/**
 * 表cosp_asyn_task_conf的增删改查
 */
public interface IAsynTaskConfService {
    /**
     * 获取所有的任务定义
     * @return 任务定义
     */
    public List<AsynTaskConf> queryAll();
}
