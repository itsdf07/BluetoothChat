package com.itsdf07.bluetoothchat.common.log;

/**
 * Log信息打印接口类
 */
public interface ILoggerPrinter {

    /**
     * @return Log设置类LogSettings
     */
    LogSettings getSettings();

    void resetSettings();

    /**
     * VERBOSE = 2;//详细 - 显示所有日志消息（默认值）
     *
     * @param tag
     * @param message
     * @param args
     */
    void v(String tag, String message, Object... args);


    /**
     * DEBUG = 3;//调试 - 显示仅在开发期间有用的调试日志消息，以及此列表中的消息级别较低
     *
     * @param tag
     * @param message
     * @param args
     */
    void d(String tag, String message, Object... args);

    /**
     * INFO = 4;//信息 - 显示常规使用的预期日志消息以及此列表中的消息级别
     *
     * @param tag
     * @param message
     * @param args
     */
    void i(String tag, String message, Object... args);

    /**
     * WARN = 5;//警告 - 显示尚未出现错误的可能问题，以及此列表中的消息级别
     *
     * @param tag
     * @param message
     * @param args
     */
    void w(String tag, String message, Object... args);

    /**
     * ERROR = 6;//错误 - 显示导致错误的问题，以及此列表中的消息级别较低
     *
     * @param tag
     * @param throwable
     * @param message
     * @param args
     */
    void e(String tag, Throwable throwable, String message, Object... args);


    /**
     * ASSERT = 7;//断言 - 显示开发人员期望永远不会发生的问题
     *
     * @param tag
     * @param throwable
     * @param message
     * @param args
     */
    void wtf(String tag, Throwable throwable, String message, Object... args);

    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    void json(String json);

    /**
     * Formats the json content and print it
     *
     * @param xml the xml content
     */
    void xml(String xml);

    /**
     * Log打印处理
     *
     * @param tag       tag
     * @param priority  Log打印级别：v、d、i、w、e、a
     * @param message   打印内容
     * @param throwable 异常信息
     */
    void log(String tag, int priority, String message, Throwable throwable);
}
