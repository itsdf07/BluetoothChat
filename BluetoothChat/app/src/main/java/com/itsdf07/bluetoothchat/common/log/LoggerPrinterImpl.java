package com.itsdf07.bluetoothchat.common.log;

import android.text.TextUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//Log打印方式1
//08-07 16:31:11.839 10750-10750/? D/rchatdebug: ╔════════════════════════════════════════════════════════════════════════════════════════
//08-07 16:31:11.839 10750-10750/? D/rchatdebug: ║ Thread: main
//08-07 16:31:11.839 10750-10750/? D/rchatdebug: ╟────────────────────────────────────────────────────────────────────────────────────────
//08-07 16:31:11.839 10750-10750/? D/rchatdebug: ║ Instrumentation.callActivityOnStart  (Instrumentation.java:1238)
//08-07 16:31:11.839 10750-10750/? D/rchatdebug: ║    BaseMvpActivity.onStart  (BaseMvpActivity.java:-1)
//08-07 16:31:11.839 10750-10750/? D/rchatdebug: ╟────────────────────────────────────────────────────────────────────────────────────────
//08-07 16:31:11.840 10750-10750/? D/rchatdebug: ║ onStart : this = com.rchat.pocmini.activity.login.LoginMvpActivity@64b8c21
//08-07 16:31:11.840 10750-10750/? D/rchatdebug: ╚════════════════════════════════════════════════════════════════════════════════════════

//Log打印方式2
//08-11 16:31:24.683 14673-14673/? D/rchatdebug: ╔════════════════════════════════════════════════════════════════════════════════════════
//08-11 16:31:24.685 14673-14673/? D/rchatdebug: ║ onStop (BaseMvpActivity.java:89) onStop : this = com.rchat.pocmini.activity.main.HomeActivity@d48c9b
//08-11 16:31:24.685 14673-14673/? D/rchatdebug: ╚════════════════════════════════════════════════════════════════════════════════════════

/**
 * Log信息打印：
 *
 * @see LogSettings.isShowThreadInfo true：Log打印方式1，false：Log打印方式2
 */
public final class LoggerPrinterImpl implements ILoggerPrinter {
    /**
     * 日期格式
     */
    private static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行
     * 保证Log不会错开打印
     */
    private static ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private static final int VERBOSE = ALog.VERBOSE;
    private static final int DEBUG = ALog.DEBUG;
    private static final int INFO = ALog.INFO;
    private static final int WARN = ALog.WARN;
    private static final int ERROR = ALog.ERROR;
    private static final int ASSERT = ALog.ASSERT;

    /**
     * Android 的单条Log打印最大限制是4076字节，所以这边限制Log块最大限制为4000字节，默认编码为UTF-8
     */
    private static final int CHUNK_SIZE = 4000;

    /**
     * It is used for json pretty print
     */
    private static final int JSON_INDENT = 2;

    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 3;

    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    /**
     * It is used to determine log settings such as method count, thread info visibility
     */
    private final LogSettings mLogSettings = new LogSettings();

    @Override
    public LogSettings getSettings() {
        return mLogSettings;
    }

    @Override
    public void resetSettings() {
        mLogSettings.reset();
    }


    @Override
    public void v(String tag, String message, Object... args) {
        log(tag, VERBOSE, null, message, args);
    }

    @Override
    public void d(String tag, String message, Object... args) {
        log(tag, DEBUG, null, message, args);
    }

    @Override
    public void i(String tag, String message, Object... args) {
        log(tag, INFO, null, message, args);
    }

    @Override
    public void w(String tag, String message, Object... args) {
        log(tag, WARN, null, message, args);
    }

    @Override
    public void e(String tag, Throwable throwable, String message, Object... args) {
        log(tag, ERROR, throwable, message, args);
    }

    @Override
    public void wtf(String tag, Throwable throwable, String message, Object... args) {
        log(tag, ASSERT, throwable, message, args);
    }

    @Override
    public void json(String json) {
        if (TextUtils.isEmpty(json)) {
            d(getSettings().getTag(), "Empty/Null json content");
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                d(getSettings().getTag(), message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                d(getSettings().getTag(), message);
                return;
            }
            e(getSettings().getTag(), null, "Invalid Json");
        } catch (JSONException e) {
            e(getSettings().getTag(), e, "Invalid Json");
        }
    }

    @Override
    public void xml(String xml) {
        if (TextUtils.isEmpty(xml)) {
            d(getSettings().getTag(), "Empty/Null xml content");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            d(getSettings().getTag(), xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e(getSettings().getTag(), e, "Invalid xml");
        }
    }

    @Override
    public synchronized void log(String tag, int priority, String message, Throwable throwable) {
        if (mLogSettings.getLogLevel() == LogLevel.NONE) {
            return;
        }
        if (throwable != null) {
            if (message != null) {
                message += " : " + LogHelper.getStackTraceString(throwable);
            } else {
                message = LogHelper.getStackTraceString(throwable);
            }
        }

        if (TextUtils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }

        int methodCount = getSettings().getMethodCount();

        logTopBorder(priority, tag);
        logHeaderContent(priority, tag, methodCount);

        //get bytes of message with system's default charset (which is UTF-8 for Android)
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        //内容打印：
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                if (mLogSettings.isShowThreadInfo()) {
                    logDivider(priority, tag);
                }
            }
            logContent(priority, tag, message);
            logBottomBorder(priority, tag);
            return;
        }
        if (methodCount > 0) {
            if (mLogSettings.isShowThreadInfo()) {
                logDivider(priority, tag);
            }
        }
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(priority, tag, new String(bytes, i, count));
        }
        logBottomBorder(priority, tag);
    }


    /**
     * Log块上边界线╔════════════════════════════════════════════════════════════════════════════════════════
     *
     * @param logType
     * @param tag
     */
    private void logTopBorder(int logType, String tag) {
        logChunk(logType, tag, TOP_BORDER);
    }

    /**
     * 打印当前Log所在线程 和 方法调用栈顺序的数量
     *
     * @param logType
     * @param tag
     * @param methodCount Log调用方法栈顺序的数量
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void logHeaderContent(int logType, String tag, int methodCount) {
        if (!mLogSettings.isShowThreadInfo()) {
            return;
        }
        logChunk(logType, tag, HORIZONTAL_DOUBLE_LINE + " Thread: " + Thread.currentThread().getName());
        logDivider(logType, tag);
        String level = "";
        /**
         * Thread.currentThread().getStackTrace()
         * 返回一个表示该线程堆栈转储的堆栈跟踪元素数组。
         * 如果该线程尚未启动或已经终止，则该方法将返回一个零长度数组。
         * 如果返回的数组不是零长度的，则其第一个元素代表堆栈顶，它是该序列中最新的方法调用。
         * 最后一个元素代表堆栈底，是该序列中最旧的方法调用。getStackTrace()[0]表示的事getStackTrace方法
         */
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        //倒序的方法堆栈中指向的方法名索引
        int stackOffset = getStackOffset(trace) + mLogSettings.getMethodOffset();

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }

        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("║ ")
                    .append(level)
                    .append(getSimpleClassName(trace[stackIndex].getClassName()))
                    .append(".")
                    .append(trace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].getFileName())
                    .append(":")
                    .append(trace[stackIndex].getLineNumber())
                    .append(")");
            level += "   ";
            logChunk(logType, tag, builder.toString());
        }
    }

    /**
     * Log块下边界线╚════════════════════════════════════════════════════════════════════════════════════════
     *
     * @param logType
     * @param tag
     */
    private void logBottomBorder(int logType, String tag) {
        logChunk(logType, tag, BOTTOM_BORDER);
    }

    /**
     * 横线绘制："════════════════════════════════════════════";
     *
     * @param logType
     * @param tag
     */
    private void logDivider(int logType, String tag) {
        logChunk(logType, tag, MIDDLE_BORDER);
    }


    /**
     * message内容块打印：使用者想要打印的内容
     *
     * @param logType
     * @param tag
     * @param chunk
     */
    private void logContent(int logType, String tag, String chunk) {
        if (!mLogSettings.isShowThreadInfo()) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            //倒序的方法堆栈中指向的方法名索引
            int stackOffset = getStackOffset(trace) + mLogSettings.getMethodOffset() - 1;
            int methodCount = getSettings().getMethodCount();
            //corresponding method count with the current stack may exceeds the stack trace. Trims the count
            if (methodCount + stackOffset > trace.length) {
                methodCount = trace.length - stackOffset - 1;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = methodCount; i > 1; i--) {
                int stackIndex = i + stackOffset;
                if (stackIndex >= trace.length) {
                    continue;
                }

                builder.append(trace[stackIndex].getMethodName())
                        .append(" (")
                        .append(trace[stackIndex].getFileName())
                        .append(":")
                        .append(trace[stackIndex].getLineNumber())
                        .append(") ");

            }
            chunk = builder.toString() + chunk;
        }
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logWrite2File("[" + logType(logType) + "]:" + tag + "::" + chunk + "\n", true);
            logChunk(logType, tag, HORIZONTAL_DOUBLE_LINE + " " + line);
        }
    }

    /**
     * Log打印
     *
     * @param logType
     * @param tag
     * @param message
     */
    private void logChunk(int logType, String tag, String message) {
        if (TextUtils.isEmpty(tag)) {
            tag = getSettings().getTag();
        }
        switch (logType) {
            case ERROR:
                mLogSettings.getLogAdapter().e(tag, message);
                break;
            case INFO:
                mLogSettings.getLogAdapter().i(tag, message);
                break;
            case VERBOSE:
                mLogSettings.getLogAdapter().v(tag, message);
                break;
            case WARN:
                mLogSettings.getLogAdapter().w(tag, message);
                break;
            case ASSERT:
                mLogSettings.getLogAdapter().wtf(tag, message);
                break;
            case DEBUG:
                // Fall through, log debug by default
            default:
                mLogSettings.getLogAdapter().d(tag, message);
                break;
        }

//        logWrite2File("[" + logType(logType) + "] " + finalTag + "->" + message + "\n", true);
    }

    /**
     * This method is synchronized in order to avoid messy of logs' order.
     */
    private synchronized void log(String tag, int priority, Throwable throwable, String msg, Object... args) {
        if (!getSettings().isLog()) {
            return;
        }
        if (mLogSettings.getLogLevel() == LogLevel.NONE) {
            return;
        }
        String message = createMessage(msg, args);
        log(tag, priority, message, throwable);
    }

    /**
     * log信息拼接
     *
     * @param message
     * @param args
     * @return
     */
    private String createMessage(String message, Object... args) {
        return args == null || args.length == 0 ? message : String.format(message, args);
    }

    /**
     * 获取类名
     *
     * @param name
     * @return
     */
    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     * 在这个类的方法调用之后，确定堆栈跟踪的起始索引。
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private int getStackOffset(StackTraceElement[] trace) {
        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            //过滤掉ALog相关类中的Log信息打印，防止Log混乱
            if (!name.equals(LoggerPrinterImpl.class.getName()) && !name.equals(ALog.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    /**
     * log的Object对象转成String
     *
     * @param obj
     * @return
     */
    public static String obj2String(Object obj) {
        String message = "Printing information is null";
        if (null != obj) {
            if (obj.getClass().isArray()) {
                message = Arrays.deepToString((Object[]) obj);
            } else {
                message = obj.toString();
            }
        }
        return message;
    }

    /**
     * 文件写入
     *
     * @param content 内容
     **/
    public synchronized static void logWrite2File(String content, final boolean append) {
//        if (!ISLOG2LOCAL) {
//            return;
//        }
//        if (content == null) {
//            return;
//        }
//        Date now = new Date();
//        String date = new SimpleDateFormat("MM-dd").format(now);
//        final String logFilePath = FileUtils.INNERSDPATH + date + ".txt";
//        final File logFile = FileUtils.getFileByPath(logFilePath);
//        if (!FileUtils.createOrExistsFile(logFile)) {
//            return;
//        }
//        String time = mSimpleDateFormat.format(now);
//        final String logContent = time + "：" + content;
//
//        Runnable syncRunnable = new Runnable() {
//            @Override
//            public void run() {
//                FileUtils.write2File(logFile, logContent, append);
//            }
//        };
//        mExecutorService.execute(syncRunnable);
    }

    /**
     * @param logType log级别
     * @return
     */
    private String logType(int logType) {
        String type;
        switch (logType) {
            case ERROR:
                type = "e";
                break;
            case INFO:
                type = "i";
                break;
            case VERBOSE:
                type = "v";
                break;
            case WARN:
                type = "w";
                break;
            case ASSERT:
                type = "wtf";
                break;
            case DEBUG:
                // Fall through, log debug by default
            default:
                type = "d";
                break;
        }
        return type;
    }
}
