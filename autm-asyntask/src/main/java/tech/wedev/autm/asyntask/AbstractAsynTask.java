package tech.wedev.autm.asyntask;

/**
 * 抽象异步任务处理类
 */
public abstract class AbstractAsynTask {
    /**
     * 获取异步任务处理线程状态
     *
     * @return 异步任务处理线程状态
     */
    public abstract ThreadStatus getStatus();
}
