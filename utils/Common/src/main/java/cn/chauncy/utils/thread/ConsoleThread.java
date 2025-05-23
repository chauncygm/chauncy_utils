package cn.chauncy.utils.thread;

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
    public static final ConsoleThread INSTANCE = new ConsoleThread();

    /** 运行状态 */
    private volatile boolean runningState = false;
    /** 命令处理器 */
    private final Map<String, Function<List<String>, Object>> handlers = new ConcurrentHashMap<>();

    private ConsoleThread() {
        this.setName("Console-Thread");
        // 设置为守护线程
        this.setDaemon(true);
        setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("ConsoleThread has uncaught exception.", e);
        });
    }

    public Map<String, Function<List<String>, Object>> getHandlers() {
        return handlers;
    }

    /**
     * 设置命令分隔符
     *
     * @param splitter 分隔符
     * @return 静态实例
     */
    public static ConsoleThread setSplitter(@NonNull String splitter) {
        ConsoleThread.COMMAND_SPLITTER = splitter;
        return INSTANCE;
    }

    /**
     * 设置命令前缀
     *
     * @param prefix 前缀
     * @return 静态实例
     */
    public static ConsoleThread setCommandPrefix(@NonNull String prefix) {
        ConsoleThread.COMMAND_PREFIX = prefix;
        return INSTANCE;
    }

    /**
     * 添加命令处理器
     *
     * @param command 命令
     * @param handler 处理器
     * @return 静态实例
     */
    @SuppressWarnings("UnusedReturnValue")
    public ConsoleThread addHandler(@NonNull String command, @NonNull Function<List<String>, Object> handler) {
        if (INSTANCE.getHandlers().containsKey(command)) {
            throw new IllegalArgumentException("command[" + command + "] has been registered.");
        }
        INSTANCE.getHandlers().put(command, handler);
        return INSTANCE;
    }

    /**
     * 添加命令处理器类
     *
     * @param clazz 处理器类
     * @return 静态实例
     */
    public ConsoleThread addHandlerClass(@NonNull Class<?> clazz) {
        // 反射获取类中匹配handler的静态方法，并加入到handlers中
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            // 检查参数：1个List<String>参数，且是静态方法
            if (Modifier.isStatic(method.getModifiers())
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == List.class) {

                // 创建符合Function接口的处理器
                Function<List<String>, Object> handler = convertToFunction(method);
                addHandler(method.getName(), handler);
            }
        }
        return INSTANCE;
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
    public void daemonStart() {
        State state = ConsoleThread.INSTANCE.getState();
        if (state == Thread.State.NEW) {
            ConsoleThread.INSTANCE.start();
            return;
        }
        logger.warn("ConsoleThread has been started, current state: {}", state);
    }

    public void terminate() {
        INSTANCE.runningState = false;
    }

    @Override
    public void run() {
        runningState = true;
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            if (!runningState) {
                break;
            }
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
            Function<List<String>, Object> handler = handlers.get(command);
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
