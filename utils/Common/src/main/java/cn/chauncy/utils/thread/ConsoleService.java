package cn.chauncy.utils.thread;

import com.google.common.util.concurrent.AbstractService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 控制台命令执行线程
 * 允许添加处理器执行控制台输入的命令
 *
 * @author chauncy
 */
public class ConsoleService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleService.class);

    private static String COMMAND_PREFIX = "#";
    private static String COMMAND_SPLITTER = " ";

    /** 运行状态 */
    private volatile boolean runningState = false;
    /** 命令处理器 */
    private static final Map<String, Function<List<String>, Object>> handlers = new ConcurrentHashMap<>();

    @Override
    protected void doStart() {
        Thread thread = new Thread(this::execute, "Console-Thread");
        thread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("ConsoleThread has uncaught exception.\n", e);
        });
        thread.start();
    }

    @Override
    protected void doStop() {
        runningState = false;
    }

    private void execute() {
        notifyStarted();
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
        notifyStopped();
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

    public static Map<String, Function<List<String>, Object>> getHandlers() {
        return handlers;
    }

    /**
     * 设置命令分隔符
     *
     * @param splitter 分隔符
     * @return 静态实例
     */
    public static void setSplitter(@NonNull String splitter) {
        ConsoleService.COMMAND_SPLITTER = splitter;
    }

    /**
     * 设置命令前缀
     *
     * @param prefix 前缀
     * @return 静态实例
     */
    public static void setCommandPrefix(@NonNull String prefix) {
        ConsoleService.COMMAND_PREFIX = prefix;
    }

    /**
     * 添加命令处理器
     *
     * @param command 命令
     * @param handler 处理器
     * @return 静态实例
     */
    @SuppressWarnings("UnusedReturnValue")
    public static void addHandler(@NonNull String command, @NonNull Function<List<String>, Object> handler) {
        if (handlers.containsKey(command)) {
            throw new IllegalArgumentException("command[" + command + "] has been registered.");
        }
        handlers.put(command, handler);
    }

    /**
     * 添加命令处理器类
     *
     * @param clazz 处理器类
     * @return 静态实例
     */
    public static void addHandlerClass(@NonNull Class<?> clazz) {
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
}
