package com.itsdf07.bluetoothchat.common.log;


import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothTools;

/**
 *
 */
public final class LogSettings {
    /**
     * Log的TAG
     */
    private String tag = BluetoothTools.TAG;
    /**
     * 是否打印Log
     */
    private boolean isLog = true;

    /**
     * 是否保存Log信息
     */
    private boolean isLog2Local = false;

    /**
     * Log本地文件存储路径
     */
    private String logFilePath = "";

    /**
     * 是否打印线程名称
     */
    private boolean isShowThreadInfo = true;

    private LogAdapterImpl logAdapter;

    /**
     * Determines to how logs will be printed
     */
    private LogLevel logLevel = LogLevel.FULL;

    private int methodCount = 2;
    private int methodOffset = 0;

    /**
     * 设置Log打印时的Tag
     *
     * @param tag
     * @return
     */
    public LogSettings setTag(String tag) {
        if (null == tag) {
            throw new NullPointerException("tag may not be null");
        }
        if (tag.trim().length() == 0) {
            throw new IllegalStateException("tag may not be empty");
        }
        this.tag = tag;
        return this;
    }

    public String getTag() {
        return tag;
    }

    /**
     * 是否打印Log
     *
     * @return
     */
    public boolean isLog() {
        return isLog;
    }

    /**
     * 设置是否打印Log日志
     *
     * @param log
     */
    public LogSettings setLog(boolean log) {
        isLog = log;
        return this;
    }

    /**
     * 是否本地保存Log信息
     *
     * @return
     */
    public boolean isLog2Local() {
        return isLog2Local;
    }

    /**
     * 设置是否本地保存Log信息
     *
     * @param log2Local
     */
    public LogSettings setLog2Local(boolean log2Local) {
        isLog2Local = log2Local;
        return this;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * 设置Log存储路径
     *
     * @param logFilePath
     */
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * 是否打印线程名称
     *
     * @return
     */
    public LogSettings hideThreadInfo() {
        isShowThreadInfo = false;
        return this;
    }

    /**
     * 是否显示线程名称：
     *
     * @return false：不显示线程信息，即只打印内容
     */
    public boolean isShowThreadInfo() {
        return isShowThreadInfo;
    }

    public LogSettings setLogAdapter(LogAdapterImpl adapter) {
        this.logAdapter = adapter;
        return this;
    }

    public LogAdapterImpl getLogAdapter() {
        if (logAdapter == null) {
            logAdapter = new LogAdapterImpl();
        }
        return logAdapter;
    }

    public LogSettings setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }


    //===============================================================================

    public LogSettings setMethodOffset(int offset) {
        this.methodOffset = offset;
        return this;
    }

    public int getMethodOffset() {
        return methodOffset;
    }

    public LogSettings setMethodCount(int methodCount) {
        if (methodCount < 0) {
            methodCount = 0;
        }
        this.methodCount = methodCount;
        return this;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void reset() {
        methodCount = 2;
        methodOffset = 0;
        isShowThreadInfo = true;
        logLevel = LogLevel.FULL;
    }
}
