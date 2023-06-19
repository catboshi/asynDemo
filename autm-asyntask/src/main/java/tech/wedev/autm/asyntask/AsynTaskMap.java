package tech.wedev.autm.asyntask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskPriorityType;

/**
 * 异步任务队列map，用于移除队列用
 */
public class AsynTaskMap {

    private Map<TaskPriorityType, ArrayList<AsynTaskWorker>> taskQueueMap;//按权限分类
    private Map<String, AsynTaskWorker> taskIdWorkerMap;//按taskId分类

    public AsynTaskMap() {
        //优先级map初始化
        taskQueueMap = new HashMap<TaskPriorityType, ArrayList<AsynTaskWorker>>();
        taskIdWorkerMap = new HashMap<String, AsynTaskWorker>();
        for (TaskPriorityType taskPriorityType :
                TaskPriorityType.values()) {
            taskQueueMap.put(taskPriorityType, new ArrayList<AsynTaskWorker>());
        }
    }

    public void clear() {
        taskIdWorkerMap.clear();
        for (TaskPriorityType taskPriorityType :
                TaskPriorityType.values()) {
            taskQueueMap.get(taskPriorityType).clear();
        }
    }

    public void addWorker(AsynTaskWorker worker) {
        taskQueueMap.get(worker.getTaskBean().getPriority()).add(worker);
        taskIdWorkerMap.put(worker.getTaskBean().getTaskId(), worker);
    }

    public void removeWorker(AsynTaskWorker worker) {
        taskQueueMap.get(worker.getTaskBean().getPriority()).remove(worker);
        taskIdWorkerMap.remove(worker.getTaskBean().getTaskId());
    }

    public void removeWorker(String taskId) {
        removeWorker(getWorker(taskId));
    }

    public AsynTaskWorker getWorker(String taskId) {
        return taskIdWorkerMap.get(taskId);
    }

    /**
     * 获取优先级最低，最后进来的worker
     * @return
     */
    public AsynTaskWorker getLowestWorker() {
        for (TaskPriorityType priority :
                TaskPriorityType.orderList) {
            if (taskQueueMap.get(priority).size() != 0) {
                ArrayList<AsynTaskWorker> workerList = taskQueueMap
                        .get(priority);
                return workerList.get(workerList.size() - 1);
            }
        }
        return null;
    }
}
