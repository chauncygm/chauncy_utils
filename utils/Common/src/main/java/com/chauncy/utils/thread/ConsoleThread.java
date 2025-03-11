package com.chauncy.utils.thread;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 控制台命令执行线程
 * 允许添加处理器执行控制台输入的命令
 *
 * @author chauncy
 */
public class ConsoleThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleThread.class);

    private static String COMMAND_PREFIX = "#";
    private static String COMMAND_SPLITTER = " ";

    /** 控制台命令线程实例 */
    private static final ConsoleThread instance = new ConsoleThread();
    /** 命令处理器 */
    private static final Map<String, Function<List<String>, Object>> HANDLERS = new ConcurrentHashMap<>();

    private ConsoleThread() {
        this.setName("Console-Thread");
        // 设置为守护线程
        this.setDaemon(true);
    }

    @Deprecated
    public static ConsoleThread getInstance() {
        return instance;
    }

    @Deprecated
    public static Map<String, Function<List<String>, Object>> getHandlers() {
        return HANDLERS;
    }

    /**
     * 设置命令分隔符
     *
     * @param splitter 分隔符
     * @return 静态实例
     */
    public static ConsoleThread setSplitter(@NonNull String splitter) {
        ConsoleThread.COMMAND_SPLITTER = splitter;
        return instance;
    }

    /**
     * 设置命令前缀
     *
     * @param prefix 前缀
     * @return 静态实例
     */
    public static ConsoleThread setCommandPrefix(@NonNull String prefix) {
        ConsoleThread.COMMAND_PREFIX = prefix;
        return instance;
    }

    /**
     * 添加命令处理器
     *
     * @param command 命令
     * @param handler 处理器
     * @return 静态实例
     */
    public static ConsoleThread addHandler(@NonNull String command, @NonNull Function<List<String>, Object> handler) {
        if (ConsoleThread.HANDLERS.containsKey(command)) {
            throw new IllegalArgumentException("command[" + command + "] has been registered.");
        }
        ConsoleThread.HANDLERS.put(command, handler);
        return instance;
    }

    /**
     * 添加命令处理器类
     *
     * @param clazz 处理器类
     * @return 静态实例
     */
    public static ConsoleThread addHandlerClass(@NonNull Class<?> clazz) {
        // 反射获取类中匹配handler的静态方法，并加入到handlers中
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            // 检查参数：1个List<String>参数，且是静态方法
            if (Modifier.isStatic(method.getModifiers())
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == List.class) {

                // 创建符合Function接口的处理器
                Function<List<String>, Object> handler = convertToFunction(method);
                ConsoleThread.addHandler(method.getName(), handler);
            }
        }
        return instance;
    }

    private static Function<List<String>, Object> convertToFunction(Method method) {
        // 调用静态方法
        return params -> {
            try {
                method.setAccessible(true);
                return method.invoke(null, params); // 调用静态方法
            } catch (Exception e) {
                logger.error("Invoke handler method failed: {}", method.getName(), e);
                return null;
            }
        };
    }

    /**
     * 启动控制台线程
     */
    public static void daemonStart() {
        if (ConsoleThread.instance.getState() == Thread.State.NEW) {
            ConsoleThread.instance.start();
        } else {
            logger.warn("ConsoleThread has been started.");
        }
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String commandStr = sc.nextLine();
            if (!commandStr.startsWith(COMMAND_PREFIX)) {
                continue;
            }
            doCommand(commandStr);
        }
    }

    /**
     * 执行命令
     *
     * @param commandStr 命令字符串
     */
    private void doCommand(String commandStr) {
        try {
            String[] split = commandStr.split(COMMAND_SPLITTER);
            String command = split[0].substring(1);
            List<String> args = Arrays.asList(split).subList(1, split.length);
            Function<List<String>, Object> handler = HANDLERS.get(command);
            if (handler == null) {
                throw new IllegalArgumentException(command);
            }
            Object result = handler.apply(args);
            logger.info("Command[{}] executed success. result: {}", command, result);
        } catch (Exception e) {
            logger.error("Command[{}] executed failed.", commandStr, e);
        }
    }
}
