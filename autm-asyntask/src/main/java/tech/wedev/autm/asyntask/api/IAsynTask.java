package tech.wedev.autm.asyntask.api;

import tech.wedev.autm.asyntask.AsynTaskBean;
import tech.wedev.autm.asyntask.AsynTaskErrInfo;

/**
 * 任务处理接口
 */
public interface IAsynTask {

    /**
     * 执行任务
     * @param task 任务对象
     * @return 执行结果
     */
    public AsynTaskErrInfo executeTask(AsynTaskBean task);
}
