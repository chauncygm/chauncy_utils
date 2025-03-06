package com.chauncy.utils.reload;


import cn.chauncy.agent.Agent;
import org.openjdk.jol.info.GraphLayout;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Objects;

/**
 * Instrumentation工具类
 *
 * @author chauncy
 */
public class InstHelper {

    private static volatile Instrumentation instrumentation;

    static {
        Instrumentation inst = Agent.getInstrumentation();
        if (inst != null) {
            InstHelper.instrumentation = inst;
        }
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        InstHelper.instrumentation = instrumentation;
    }

    /**
     * 获取指定对象在内存中的大小，包含自身及其他引用对象
     *
     * @param object 指定对象
     * @return  对象size
     */
    public static long getDeepObjectSize(Object object) {
        Objects.requireNonNull(object, "obj is null");
        GraphLayout graphLayout = GraphLayout.parseInstance(object);
        return graphLayout.totalSize();
    }

    /**
     * 获取指定对象在内存中的大小，仅仅包含浅层对象
     *
     * @param object 指定对象
     * @return 对象size
     */
    public static long getObjectSize(Object object) {
        Objects.requireNonNull(instrumentation, "Instrumentation is not set");
        Objects.requireNonNull(object, "obj is null");
        return instrumentation.getObjectSize(object);
    }

    /**
     * 判断是否支持类重定义功能
     */
    public static boolean isRedefineSupported() {
        Objects.requireNonNull(instrumentation, "Instrumentation is not set");
        return instrumentation.isRedefineClassesSupported();
    }


    /**
     * 判断指定类是否支持类重定义，与{@link #isRedefineSupported()}没有直接关联
     */
    public static boolean isModifiable(Class<?> theClass) {
        Objects.requireNonNull(instrumentation, "Instrumentation is not set");
        return instrumentation.isModifiableClass(theClass);
    }

    /**
     * 重定义指定类文件，该类需同时满足 {@link #isModifiable(Class)} 和 {@link #isRedefineSupported()}
     */
    public static void redefineClasses(ClassDefinition... definitions) throws UnmodifiableClassException, ClassNotFoundException {
        Objects.requireNonNull(instrumentation, "Instrumentation is not set");
        instrumentation.redefineClasses(definitions);
    }
}
