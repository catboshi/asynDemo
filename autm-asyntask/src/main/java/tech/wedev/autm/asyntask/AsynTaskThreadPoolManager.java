package tech.wedev.autm.asyntask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务池管理类
 */
public class AsynTaskThreadPoolManager {

    private static ThreadLocal<ArrayList<AsynTaskWorker>> THREADLOCAL_TASK_WORKER = new ThreadLocal<ArrayList<AsynTaskWorker>>();//线程内缓存待执行任务
    private static Map<String, AsynTaskThreadPool> TYPE_POOL = new HashMap<String, AsynTaskThreadPool>();//任务类型 任务池映射

    /**
     * 添加任务池
     *
     * @param taskType
     * @param threadPool
     */
    public static void addPool(String taskType, AsynTaskThreadPool threadPool) {
        TYPE_POOL.put(taskType, threadPool);
    }

    /**
     * 联机任务添加到异步任务队列
     *
     * @param commitFlag
     */
    public static void commitThreadLocalTask(boolean commitFlag) {
        if (THREADLOCAL_TASK_WORKER.get() != null
                && THREADLOCAL_TASK_WORKER.get().size() != 0) {
            if (commitFlag) {
                for (AsynTaskWorker worker :
                        THREADLOCAL_TASK_WORKER.get()) {
                    TYPE_POOL.get(worker.getAsynTaskInfo().getGetTaskType()).execute(worker);
                }
            }
            THREADLOCAL_TASK_WORKER.get().clear();
        }
    }

    /**
     * 清空线程本地化对象缓存，事务开始时调用
     */
    public static void clearThreadLocalTask() {
        if (THREADLOCAL_TASK_WORKER.get() != null
                && THREADLOCAL_TASK_WORKER.get().size() != 0) {
            THREADLOCAL_TASK_WORKER.get().clear();
        }
    }

    public static void addPendingWorker(AsynTaskWorker worker) {
        if (THREADLOCAL_TASK_WORKER.get() != null) {
            THREADLOCAL_TASK_WORKER.set(new ArrayList<AsynTaskWorker>());
        }
        THREADLOCAL_TASK_WORKER.get().add(worker);
    }
}
