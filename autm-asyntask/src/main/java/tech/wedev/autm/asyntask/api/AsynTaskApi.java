package tech.wedev.autm.asyntask.api;

import tech.wedev.autm.asyntask.AsynTaskBean;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskPriorityType;
import tech.wedev.autm.asyntask.AsynTaskEnum.TaskStateType;
import tech.wedev.autm.asyntask.AsynTaskInfo;
import tech.wedev.autm.asyntask.AsynTaskThreadPool;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 异步任务API
 */
public class AsynTaskApi {

    /**
     * 将异步任务插入待处理表
     *
     * @param taskType    任务类型，必须与cosp_asyn_task_def表中一致
     * @param subTaskType 子任务类型，必须与cosp_asyn_task_conf表中一致
     * @param fk          业务主键
     * @param fk1         业务主键1，可为null
     * @param fk2         业务主键2，可为null
     * @param fk3         业务主键3，可为null
     * @param planTime    计划执行时间，可为null，格式yyyy-MM-dd HH24:mi:ss
     * @param priority    计划优先级，可为null
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean pushTask(String taskType, String subTaskType,
                                   String fk, String fk1, String fk2, String fk3,
                                   String planTime, TaskPriorityType priority) throws Throwable {

        //构造对象
        AsynTaskBean taskBean = new AsynTaskBean();
        taskBean.setTaskType(taskType);
        taskBean.setSubTaskType(subTaskType);
        taskBean.setKeywords(fk);
        taskBean.setRefCol1(fk1);
        taskBean.setRefCol2(fk2);
        taskBean.setRefCol3(fk3);
        taskBean.setPlanTime(planTime);
        taskBean.setPriority(priority == null ? TaskPriorityType.NORMAL : priority);

        return pushTask(taskBean);
    }

    /**
     * 将异步任务插入待处理表
     *
     * @param taskType    任务类型，必须与cosp_asyn_task_def表中一致
     * @param subTaskType 子任务类型，必须与cosp_asyn_task_conf表中一致
     * @param fk          业务主键
     * @param fk1         业务主键1，可为null
     * @param fk2         业务主键2，可为null
     * @param fk3         业务主键3，可为null
     * @param planTime    计划执行时间，可为null，格式yyyy-MM-dd HH24:mi:ss
     * @param priority    计划优先级，可为null
     * @param info1       备注信息1，可为null
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean pushTask(String taskType, String subTaskType,
                                   String fk, String fk1, String fk2, String fk3,
                                   String planTime, TaskPriorityType priority, String info1) throws Throwable {
        //构造对象
        AsynTaskBean taskBean = new AsynTaskBean();
        taskBean.setTaskType(taskType);
        taskBean.setSubTaskType(subTaskType);
        taskBean.setKeywords(fk);
        taskBean.setRefCol1(fk1);
        taskBean.setRefCol2(fk2);
        taskBean.setRefCol3(fk3);
        taskBean.setPlanTime(planTime);
        taskBean.setPriority(priority == null ? TaskPriorityType.NORMAL : priority);
        taskBean.setInfo1(info1);

        return pushTask(taskBean);
    }

    /**
     * 将异步任务插入待处理表
     *
     * @param taskBean 待处理任务对象
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean pushTask(AsynTaskBean taskBean) throws Throwable {
        //如果已启动任务，则直接调用方法
        AsynTaskThreadPool threadPool = AsynTaskModule.getInstance().taskPools
                .get(taskBean.getTaskType());
        if (threadPool != null) {
            return 1 == threadPool.addOnlyAsynTask(taskBean);
        }

        //如果未启动，则先获取任务信息
        return false;
    }

    /**
     * 将异步任务插入待处理表，并立刻在本机排队，等待执行
     *
     * @param taskType    任务类型，必须与任务定义表中一致
     * @param subTaskType 子任务类型，必须与cosp_asyn_task_conf表中一致
     * @param fk          业务主键
     * @param fk1         业务主键1，可为null
     * @param fk2         业务主键2，可为null
     * @param fk3         业务主键3，可为null
     * @param planTime    计划执行时间，可为null，格式yyyy-MM-dd HH24:mi:ss
     * @param priority    计划优先级，可为null
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean executeTask(String taskType, String subTaskType,
                                      String fk, String fk1, String fk2, String fk3,
                                      String planTime, TaskPriorityType priority) throws Throwable {

        //构造对象
        AsynTaskBean taskBean = new AsynTaskBean();
        taskBean.setTaskType(taskType);
        taskBean.setSubTaskType(subTaskType);
        taskBean.setKeywords(fk);
        taskBean.setRefCol1(fk1);
        taskBean.setRefCol2(fk2);
        taskBean.setRefCol3(fk3);
        taskBean.setPlanTime(planTime);
        taskBean.setPriority(priority == null ? TaskPriorityType.NORMAL : priority);

        return executeTask(taskBean);
    }

    /**
     * 将异步任务插入待处理表，并立刻在本机排队，等待执行
     *
     * @param taskBean 待处理任务对象
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean executeTask(AsynTaskBean taskBean) throws Throwable {
        //如果已启动任务，则直接调用方法
        AsynTaskThreadPool threadPool = AsynTaskModule.getInstance().taskPools
                .get(taskBean.getTaskType());
        if (threadPool != null) {
            return 1 == threadPool.addAsynTask(taskBean);
        }

        //如果未启动，则先获取任务信息
        return false;
    }

    /**
     * 将异步任务插入待处理表，并立刻执行（移除低优先级任务）
     *
     * @param taskType    任务类型，必须与任务定义表中一致
     * @param subTaskType 子任务类型，必须与cosp_asyn_task_conf表中一致
     * @param fk          业务主键
     * @param fk1         业务主键1，可为null
     * @param fk2         业务主键2，可为null
     * @param fk3         业务主键3，可为null
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean forceExecuteTask(String taskType, String subTaskType,
                                           String fk, String fk1, String fk2, String fk3) throws Throwable {
        //构造对象
        AsynTaskBean taskBean = new AsynTaskBean();
        taskBean.setTaskType(taskType);
        taskBean.setSubTaskType(subTaskType);
        taskBean.setKeywords(fk);
        taskBean.setRefCol1(fk1);
        taskBean.setRefCol2(fk2);
        taskBean.setRefCol3(fk3);

        return forceExecuteTask(taskBean);
    }

    /**
     * 将异步任务插入待处理表，并立刻执行（移除低优先级任务）
     *
     * @param taskBean 待处理任务对象
     * @return 是否插入成功
     * @throws Throwable
     */
    public static boolean forceExecuteTask(AsynTaskBean taskBean) throws Throwable {
        //如果已启动任务，则直接调用方法
        AsynTaskThreadPool threadPool = AsynTaskModule.getInstance().taskPools
                .get(taskBean.getTaskType());
        if (threadPool != null) {
            return 1 == threadPool.addOnlineTask(taskBean);
        }

        //如果未启动，则先获取任务信息
        return false;
    }

    /**
     * 重做任务：将任务重新插入待处理表
     * 处理逻辑：
     * 1.判断记录是否超出重做次数
     * 2.记录的处理次数+1
     * 3.根据重做时间间隔设置，设置计划执行时间
     * 4.将任务重新插入待处理表
     *
     * @param taskBean 任务对象
     * @return 是否处理成功
     * @throws Throwable
     */
    public static boolean redoTask(AsynTaskBean taskBean) throws Throwable {
        AsynTaskInfo taskInfo = AsynTaskModule.getInstance().taskDefs
                .get(taskBean.getTaskType());

        //超出最大重做次数
        if (taskBean.getDealNum().intValue() >= taskInfo.getRedoNum()) {
            return false;
        }

        //重做时间间隔
        int redoTimeSplit = taskInfo.getRedoTimeSplit();
        taskBean.setPlanTime(null);
        if (redoTimeSplit > 0) {
            taskBean.setPlanTimeSeconds(new BigDecimal(redoTimeSplit));
        } else {
            taskBean.setPlanTimeSeconds(null);
        }
        taskBean.setDealNum(taskBean.getDealNum().add(new BigDecimal(1)));
        taskBean.setState(TaskStateType.PEDDING.getCode());
        taskBean.setIp(null);
        taskBean.setInQueueTime(null);

        return 1 == (Integer) AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().insertAsynTask(taskBean);
    }

    /**
     * 批量插入异步任务
     *
     * @param asynTaskBeans
     * @throws Throwable
     */
    public void batchPushTask(List<AsynTaskBean> asynTaskBeans) throws Throwable {
        List<AsynTaskBean> asynTaskBeanList = parseDefaultTask(asynTaskBeans);
        AbstractAsynTaskServiceFactory.getFactory().getAsynTaskDtlService().batchInsertAsynTask(asynTaskBeanList);
    }

    public boolean pushOneTask(AsynTaskBean asynTaskBean) throws Throwable {
        AsynTaskBean asynTask = parseOneDefaultTask(asynTaskBean);
        return executeTask(asynTask);
    }

    private List<AsynTaskBean> parseDefaultTask(List<AsynTaskBean> asynTaskBeanList) {
        if (asynTaskBeanList != null && !asynTaskBeanList.isEmpty()) {
            for (AsynTaskBean asynTaskBean : asynTaskBeanList) {
                parseOneDefaultTask(asynTaskBean);
            }
        }
        return asynTaskBeanList;
    }

    private AsynTaskBean parseOneDefaultTask(AsynTaskBean asynTaskBean) {
        if (asynTaskBean.getPriority() == null) {
            asynTaskBean.setPriority(TaskPriorityType.NORMAL);
        }
        if (asynTaskBean.getState() == null) {
            asynTaskBean.setState(TaskStateType.PEDDING.getCode());
        }
        if (asynTaskBean.getDealNum() == null) {
            asynTaskBean.setDealNum(new BigDecimal(0));
        }
        if (asynTaskBean.getTaskId() == null) {
            asynTaskBean.setTaskId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        return asynTaskBean;
    }
}
