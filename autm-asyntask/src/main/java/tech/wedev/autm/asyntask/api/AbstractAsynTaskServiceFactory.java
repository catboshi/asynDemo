package tech.wedev.autm.asyntask.api;

import tech.wedev.autm.asyntask.service.IAsynTaskConfService;
import tech.wedev.autm.asyntask.service.IAsynTaskDefService;
import tech.wedev.autm.asyntask.service.IAsynTaskDtlService;

public abstract class AbstractAsynTaskServiceFactory {
    private static AbstractAsynTaskServiceFactory factory = null;

    public static AbstractAsynTaskServiceFactory getFactory() {
        return factory;
    }

    protected static void setFactory(AbstractAsynTaskServiceFactory newFactory) {
        factory = newFactory;
    }

    /**
     * 表cosp_asyn_task_dtl的增删改查
     */
    public abstract IAsynTaskDtlService getAsynTaskDtlService();

    public abstract IAsynTaskConfService getAsynTaskConfService();

    public abstract IAsynTaskDefService getAsynTaskDefService();

    public abstract <T extends IAsynTask> T getIAsynTask(String serviceName) throws Exception;
}
