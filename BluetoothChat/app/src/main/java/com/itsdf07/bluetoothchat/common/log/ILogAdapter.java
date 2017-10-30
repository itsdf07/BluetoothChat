package com.itsdf07.bluetoothchat.common.log;

/**
 * Log日志打印的接口
 */
public interface ILogAdapter {
    /**
     * 详细 - 显示所有日志消息（默认值）
     *
     * @param tag
     * @param message
     */
    void v(String tag, String message);

    /**
     * 调试 - 显示仅在开发期间有用的调试日志消息，以及此列表中的消息级别较低
     *
     * @param tag
     * @param message
     */
    void d(String tag, String message);

    /**
     * 信息 - 显示常规使用的预期日志消息以及此列表中的消息级别
     *
     * @param tag
     * @param message
     */
    void i(String tag, String message);

    /**
     * 错误 - 显示导致错误的问题，以及此列表中的消息级别较低
     *
     * @param tag
     * @param message
     */
    void w(String tag, String message);

    /**
     * 错误 - 显示导致错误的问题，以及此列表中的消息级别较低
     *
     * @param tag
     * @param message
     */
    void e(String tag, String message);

    /**
     * 断言 - 显示开发人员期望永远不会发生的问题
     *
     * @param tag
     * @param message
     */
    void wtf(String tag, String message);
}