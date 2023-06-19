package tech.wedev.autm.asyntask;

/**
 * 异步任务错误信息类
 */
public class AsynTaskErrInfo {
    private boolean succFlag;
    private boolean redoFlag;
    private String errCode;
    private String errMsg;
    private Throwable throwable;

    /**
     * 默认成功，不需要重做
     */
    public AsynTaskErrInfo() {
        this.succFlag = true;
        this.redoFlag = false;
        this.errCode = "";
        this.errMsg = "";
    }

    public AsynTaskErrInfo(boolean succFlag, boolean redoFlag, String errCode, String errMsg, Throwable throwable) {
        this.succFlag = succFlag;
        this.redoFlag = redoFlag;
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.throwable = throwable;
    }

    public boolean isSuccFlag() {
        return succFlag;
    }

    public void setSuccFlag(boolean succFlag) {
        this.succFlag = succFlag;
    }

    public boolean isRedoFlag() {
        return redoFlag;
    }

    public void setRedoFlag(boolean redoFlag) {
        this.redoFlag = redoFlag;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
