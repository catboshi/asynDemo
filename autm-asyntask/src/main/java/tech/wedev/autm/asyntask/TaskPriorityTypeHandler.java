package tech.wedev.autm.asyntask;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tech.wedev.autm.asyntask.AsynTaskEnum.TaskPriorityType;

public class TaskPriorityTypeHandler implements TypeHandler<TaskPriorityType> {
    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, TaskPriorityType taskPriorityType, JdbcType jdbcType)
            throws SQLException {
        preparedStatement.setBigDecimal(i, taskPriorityType.getValue());
    }

    @Override
    public TaskPriorityType getResult(ResultSet resultSet, String s)
            throws SQLException {
        return TaskPriorityType.getEnum(resultSet.getBigDecimal(s));
    }

    @Override
    public TaskPriorityType getResult(ResultSet resultSet, int columnIndex)
            throws SQLException {
        return TaskPriorityType.getEnum(resultSet.getBigDecimal(columnIndex));
    }

    @Override
    public TaskPriorityType getResult(CallableStatement callableStatement, int columnIndex)
            throws SQLException {
        return TaskPriorityType.getEnum(callableStatement.getBigDecimal(columnIndex));
    }
}
