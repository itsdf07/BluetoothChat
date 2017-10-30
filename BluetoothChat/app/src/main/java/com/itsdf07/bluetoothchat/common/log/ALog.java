package com.itsdf07.bluetoothchat.common.log;

/**
 * ALog 是基于{@link android.util.Log}的封装，但是使用更简单，信息查看更强大
 */
public final class ALog {
    /**
     * 详细 - 显示所有日志消息（默认值）
     */
    public static final int VERBOSE = 2;
    /**
     * 调试 - 显示仅在开发期间有用的调试日志消息，以及此列表中的消息级别较低
     */
    public static final int DEBUG = 3;
    /**
     * 信息 - 显示常规使用的预期日志消息以及此列表中的消息级别
     */
    public static final int INFO = 4;
    /**
     * 警告 - 显示尚未出现错误的可能问题，以及此列表中的消息级别
     */
    public static final int WARN = 5;
    /**
     * 错误 - 显示导致错误的问题，以及此列表中的消息级别较低
     */
    public static final int ERROR = 6;
    /**
     * 断言 - 显示开发人员期望永远不会发生的问题
     */
    public static final int ASSERT = 7;

    private static LoggerPrinterImpl mLogPrinter = new LoggerPrinterImpl();

    /**
     * 初始化ALog相关：
     */
    public static LogSettings init() {
        if (mLogPrinter == null) {
            mLogPrinter = new LoggerPrinterImpl();
        }
        return mLogPrinter.getSettings();
    }

    public static boolean isLog() {
        return init().isLog();
    }

    /**
     * ALog.v("isFIleExist = %s, innerBasePath = %s", isFileExist, innerBasePath);
     *
     * @param message 要打印的内容
     * @param args    打印信息中的动态数据
     */
    public static void v(String message, Object... args) {
        vTag(mLogPrinter.getSettings().getTag(), message, args);
    }

    /**
     * ALog.v("dfsu", "isFIleExist = %s, innerBasePath = %s", isFileExist, innerBasePath);
     *
     * @param tag     tag
     * @param message 要打印的内容
     * @param args    打印信息中的动态数据
     */
    public static void vTag(String tag, String message, Object... args) {
        mLogPrinter.v(tag, message, args);
    }

    /**
     * ALog.d("isFIleExist = %s, innerBasePath = %s", isFileExist, innerBasePath);
     *
     * @param message 要打印的内容
     * @param args    打印信息中的动态数据
     */
    public static void d(String message, Object... args) {
        dTag(mLogPrinter.getSettings().getTag(), message, args);
    }

    /**
     * ALog.d("dfsu", "isFIleExist = %s, innerBasePath = %s", isFileExist, innerBasePath);
     *
     * @param tag     tag
     * @param message 要打印的内容
     * @param args    打印信息中的动态数据
     */
    public static void dTag(String tag, String message, Object... args) {
        mLogPrinter.d(tag, message, args);
    }

    public static void i(String message, Object... args) {
        iTag(mLogPrinter.getSettings().getTag(), message, args);
    }

    public static void iTag(String tag, String message, Object... args) {
        mLogPrinter.i(tag, message, args);
    }

    public static void w(String message, Object... args) {
        wTag(mLogPrinter.getSettings().getTag(), message, args);
    }

    public static void wTag(String tag, String message, Object... args) {
        mLogPrinter.w(tag, message, args);
    }

    public static void e(String message, Object... args) {
        eTag(mLogPrinter.getSettings().getTag(), message, args);
    }

    public static void eTag(String tag, String message, Object... args) {
        e(tag, null, message, args);
    }

    public static void e(String tag, Throwable throwable, String message, Object... args) {
        mLogPrinter.e(tag, throwable, message, args);
    }

    public static void wtf(String message, Object... args) {
        wtfTag(mLogPrinter.getSettings().getTag(), message, args);
    }

    public static void wtfTag(String tag, String message, Object... args) {
        wtf(tag, null, message, args);
    }

    public static void wtf(String tag, Throwable throwable, String message, Object... args) {
        mLogPrinter.wtf(tag, throwable, message, args);
    }

    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    public static void json(String json) {
        mLogPrinter.json(json);
    }

    /**
     * Formats the json content and print it
     *
     * @param xml the xml content
     */
    public static void xml(String xml) {
        mLogPrinter.xml(xml);
    }

}
