package tech.wedev.autm.asyntask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AsynTaskEnum {
    /**
     * 异步任务枚举类
     */
    public enum TaskPriorityType {
        FIRST(new BigDecimal(10)), HIGH(new BigDecimal(8)), NORMAL(new BigDecimal(6)), LOW(new BigDecimal(4)), LAST(new BigDecimal(1));

        private BigDecimal value;

        private TaskPriorityType(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getValue() {
            return value;
        }

        public static TaskPriorityType getEnum(BigDecimal value) {
            for (TaskPriorityType taskPriorityType : TaskPriorityType.values()) {
                if (taskPriorityType.value.compareTo(value) == 0) {
                    return taskPriorityType;
                }
            }
            return NORMAL;
        }

        public static final List<TaskPriorityType> orderList = new ArrayList<TaskPriorityType>();

        static {
            orderList.add(TaskPriorityType.LAST);
            orderList.add(TaskPriorityType.LOW);
            orderList.add(TaskPriorityType.NORMAL);
            orderList.add(TaskPriorityType.HIGH);
            orderList.add(TaskPriorityType.FIRST);
        }
    }

    public enum TaskStateType {
        //任务状态，0待处理 1进入队列 2开始工作 3任务成功 4任务失败 5超时被杀 6超时被抢 7任务撤销
        PEDDING("0", "待处理"), IN_QUEUE("1", "进入队列"), START_WORK("2", "开始工作"), SUCC("3", "任务成功"), FAIL("4", "任务失败"), TIME_OUT_KILL("5", "超时被杀"),
        TIME_OUT_LOCK("6", "超时被抢"), REVOKE("7", "任务撤销");
        private String code;
        private String desc;

        private TaskStateType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static TaskStateType getEnum(String code) {
            for (TaskStateType taskStateType : TaskStateType.values()) {
                if (taskStateType.code.compareTo(code) == 0) {
                    return taskStateType;
                }
            }
            return null;
        }
    }
}
