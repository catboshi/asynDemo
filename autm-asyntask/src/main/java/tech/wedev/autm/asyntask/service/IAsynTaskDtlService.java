package tech.wedev.autm.asyntask.service;

import tech.wedev.autm.asyntask.AsynTaskBean;
import tech.wedev.autm.asyntask.entity.AsynTaskConf;

import java.util.List;

/**
 * 表cosp_asyn_task_dtl的增删改查
 */
public interface IAsynTaskDtlService {
    /**
     * 获取所有的任务定义
     *
     * @return 任务定义
     */
    public List<AsynTaskConf> queryAll();

    /**
     * 新增异步任务记录
     *
     * @param taskBean 任务对象
     * @return 实际新增的记录数
     */
    public int insertAsynTask(AsynTaskBean taskBean);

    /**
     * 本机抢占任务
     *
     * @param ip             机器IP
     * @param taskType       任务类型
     * @param fetchTaskCount 每次获取的任务数限制
     * @return 抢占的任务数
     */
    public int updateStateWhenRobTask(String ip, String taskType, int fetchTaskCount);

    /**
     * 获取当前机器抢占的待处理任务列表
     *
     * @param ip       机器IP
     * @param taskType 任务类型
     * @return 待处理任务列表
     */
    public List<AsynTaskBean> queryRobList(String ip, String taskType);

    /**
     * 将取出的待处理任务状态更新为进入队列
     *
     * @param ip       机器IP
     * @param taskType 任务类型
     * @return 更新的记录数
     */
    public int updateRobListAfterPutInQueue(String ip, String taskType);

    /**
     * 更新记录的状态为开始
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateStateToStart(AsynTaskBean taskBean);

    /**
     * 更新记录的状态为处理成功
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateStateToSuccess(AsynTaskBean taskBean);

    /**
     * 更新记录的状态为处理失败
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateStateToFail(AsynTaskBean taskBean);

    /**
     * 更新记录的状态为取消
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateStateToCancel(AsynTaskBean taskBean);

    /**
     * 更新记录的状态为待处理
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateStateToPending(AsynTaskBean taskBean);

    /**
     * 更新本机的记录的状态为待处理
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateMyIpTaskStateToPending(AsynTaskBean taskBean);

    /**
     * 更新记录的状态为超时被杀
     *
     * @param taskBean 任务对象
     * @return 实际更新的记录数
     */
    public int updateStateToKill(AsynTaskBean taskBean);

    /**
     * 更新记录的状态为超时被抢
     *
     * @param ip       本机IP
     * @param taskType 任务类型
     * @return 更新的记录数
     */
    public int updateStateAndIpWhenDoubleTimeout(String ip, String taskType);

    /**
     * 获取当前机器的超时任务列表
     *
     * @param ip       机器IP
     * @param taskType 任务类型
     * @return 待处理任务列表
     */
    public List<AsynTaskBean> queryMyIpTimeoutTaskList(String ip, String taskType);

    /**
     * 将执行成功的任务插入任务明细历史表
     *
     * @param taskId 任务编号
     * @return 实际新增的记录数
     */
    public int insertSuccessTaskToHis(String taskId);

    /**
     * 将执行成功的任务插入任务明细历史表
     *
     * @param taskId 任务编号
     * @return 实际删除的记录数
     */
    public int deleteSuccessTask(String taskId);

    /**
     * 将执行成功/失败的任务插入任务明细历史表
     *
     * @param taskId 任务编号
     * @return 实际新增的记录数
     */
    public int insertTaskToHis(String taskId);

    /**
     * 将执行成功/失败的任务插入任务明细历史表
     *
     * @param taskId 任务编号
     * @return 实际删除的记录数
     */
    public int deleteTask(String taskId);

    /**
     * 批量新增异步任务
     *
     * @param asynTaskBeanList
     */
    public void batchInsertAsynTask(List<AsynTaskBean> asynTaskBeanList);
}
