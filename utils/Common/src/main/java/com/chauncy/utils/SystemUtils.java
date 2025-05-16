package com.chauncy.utils;

import com.chauncy.utils.common.ExceptionUtils;
import io.netty.util.internal.PlatformDependent;
import org.apache.commons.lang3.ArrayUtils;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.*;
import java.lang.reflect.Field;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Handler;
import java.util.logging.LogManager;

public class SystemUtils {

    public static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static final long KB_BYTE_SIZE = 1024;
    private static final long MB_BYTE_SIZE = 1024 * 1024;
    private static final long GB_BYTE_SIZE = 1024 * 1024 * 1024;

    private SystemUtils() {
    }

    //region system info
    public static String getJVMInfo() {
        return String.format("JVM: %s (vendor: %s, version: %s)",
                System.getProperty("java.vm.name"),     // 如 "OpenJDK 64-Bit Server VM"
                System.getProperty("java.vendor"),      // 如 "Oracle Corporation"
                System.getProperty("java.version")      // 如 "17.0.8"
        );
    }

    public static String getInputArguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> args = runtimeMXBean.getInputArguments();
        return "Input Arguments:" + Arrays.toString(args.toArray());
    }

    public static String getSystemProperties() {
        StringJoiner propertiesJoiner = new StringJoiner("\n\t", "System Properties:\n\t", "");
        System.getProperties().forEach((k, v) -> {
            propertiesJoiner.add(k + "=" + v);
        });
        return propertiesJoiner.toString();
    }

    public static String getClassPath() {
        return "ClassPath:" + System.getProperties().get("java.class.path").toString();
    }

    public static int getProcessId() {
        try {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            return Integer.parseInt(processName.split("@")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getRuntimeInfo() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return String.format("PID: %s | Processors: %d | Memory: free=%dMB, total=%dMB, max=%dMB",
                runtimeMXBean.getName().split("@")[0],
                runtime.availableProcessors(),
                runtime.freeMemory() / MB_BYTE_SIZE,
                runtime.totalMemory() / MB_BYTE_SIZE,
                runtime.maxMemory() / MB_BYTE_SIZE
        );
    }

    public static String getThreadInfo() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        String summery = String.format("Threads: live=%d, daemon=%d, peak=%d, total started=%d",
                bean.getThreadCount(),
                bean.getDaemonThreadCount(),
                bean.getPeakThreadCount(),
                bean.getTotalStartedThreadCount()
        );

        ThreadInfo[] threadInfos = bean.dumpAllThreads(true, true);
        StringJoiner threadInfosJoiner = new StringJoiner("\n\n", "", "");
        for (ThreadInfo threadInfo : threadInfos) {
            threadInfosJoiner.add(getThreadInfoSummery(threadInfo));
        }
        return summery + "\n" + threadInfosJoiner;
    }

    public static String getThreadInfoSummery(ThreadInfo threadInfo) {
        StringJoiner threadInfoJoiner = new StringJoiner(" ", "", "");
        threadInfoJoiner.add("\"" + threadInfo.getThreadName() + "\"");
        threadInfoJoiner.add("#" + threadInfo.getThreadId());
        threadInfoJoiner.add(threadInfo.isDaemon() ? "daemon" : "-");
        threadInfoJoiner.add(threadInfo.isInNative() ? "inNative" : "-");
        threadInfoJoiner.add(threadInfo.isSuspended() ? "suspended" : "-");
        threadInfoJoiner.add("waitedTime\\Count=" + threadInfo.getWaitedTime() + "\\" + threadInfo.getWaitedCount());
        threadInfoJoiner.add("prio=" + threadInfo.getPriority());
        threadInfoJoiner.add("state=" + threadInfo.getThreadState().toString());
        if (threadInfo.getLockName() != null || threadInfo.getLockInfo() != null) {
            threadInfoJoiner.add("\nlock=" + threadInfo.getLockName() + "|" + threadInfo.getLockInfo()
                    + "(" + threadInfo.getLockOwnerId() + "|" + threadInfo.getLockOwnerName() + ")");
        }
        if (!ArrayUtils.isEmpty(threadInfo.getStackTrace())) {
            StringJoiner stackJoiner = new StringJoiner("\n\t", "stack:\n\t", "");
            StackTraceElement[] stackTrace = threadInfo.getStackTrace();
            for (int i = 0, length = Math.min(10, stackTrace.length); i < length; i++) {
                StackTraceElement stackTraceElement = stackTrace[i];
                stackJoiner.add(stackTraceElement.toString());
            }
            threadInfoJoiner.add(stackJoiner.toString());
        }
        return threadInfoJoiner.toString();
    }

    public static String getNetworkInfo() {
        StringJoiner info = new StringJoiner("\n");
        info.add("Network Interfaces:");

        // 获取所有网络接口
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }

                StringJoiner ifInfo = new StringJoiner("\n\t", "\t", "");
                ifInfo.add("Interface: " + ni.getDisplayName());
                ifInfo.add("Status: " + (ni.isUp() ? "UP" : "DOWN"));

                // MAC 地址格式化
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    String macStr = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
                    ifInfo.add("MAC: " + macStr);
                }

                // IP 地址信息
                for (InetAddress inet : Collections.list(ni.getInetAddresses())) {
                    String ipInfo = String.format("IP: %s/%d (%s)", inet.getHostAddress(), getPrefixLength(ni, inet),
                            inet instanceof Inet6Address ? "IPv6" : "IPv4");
                    ifInfo.add(ipInfo);
                }
                info.add(ifInfo.toString());
            }

            // 补充系统级网络参数
            info.add("\nSystem Network Parameters:");
            info.add("\tIPv6 Preferred: " + System.getProperty("java.net.preferIPv6Addresses"));
            info.add("\tHTTP Proxy: " + System.getProperty("http.proxyHost"));
            return info.toString();
        } catch (SocketException e) {
            logger.error("Get network info error.", e);
            return "Get network info error.";
        }
    }

    // 获取子网前缀长度
    private static int getPrefixLength(NetworkInterface ni, InetAddress inet) {
        return ni.getInterfaceAddresses().stream()
                .filter(ia -> ia.getAddress().equals(inet))
                .findFirst()
                .map(java.net.InterfaceAddress::getNetworkPrefixLength)
                .orElse((short) -1);
    }

    // 获取直接内存使用量
    public static long getDirectMemoryUsage() {
        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        long directMemoryUsage = pools.stream()
                .filter(p -> p.getName().equals("direct"))
                .mapToLong(BufferPoolMXBean::getMemoryUsed)
                .sum();
        long nettyDirectMemoryUsage = PlatformDependent.usedDirectMemory();
        return directMemoryUsage + nettyDirectMemoryUsage;
    }
    //endregion

    //region object layout
    public static String getObjectLayout(Object obj) {
        GraphLayout graphLayout = GraphLayout.parseInstance(obj);
        return graphLayout.toPrintable();
    }

    public static String getClassLayout(Class<?> clazz) {
        ClassLayout classLayout = ClassLayout.parseClass(clazz);
        return classLayout.toPrintable();
    }

    public static String getClassLayOut(Object obj) {
        if (obj instanceof Class) {
            return getClassLayout((Class<?>) obj);
        }
        ClassLayout classLayout = ClassLayout.parseInstance(obj);
        return classLayout.toPrintable();
    }
    //endregion

    //region logger setting
    public static void setJULLogger() {
        setJULLogger(SystemUtils.class.getResourceAsStream("/logging.properties"));
    }

    public static void setJULLogger(InputStream is) {
        if (is == null) {
            logger.warn("Not found logging.properties");
            return;
        }
        try {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            ExceptionUtils.rethrow(e);
        }
    }

    public static void printLoggerSetting() {
        Handler[] handlers = java.util.logging.Logger.getLogger("").getHandlers();
        for (Handler handler : handlers) {
            logger.info("Handler: {}, Level: {}", handler.getClass().getName(), handler.getLevel());
        }
    }
    //endregion
}
