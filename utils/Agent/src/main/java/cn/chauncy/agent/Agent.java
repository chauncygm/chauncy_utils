package cn.chauncy.agent;

import java.lang.instrument.Instrumentation;

/**
 * Instrumentation开发指南
 * <h3>热更新限制</h3>
 * 1. 热更新只可以更改方法体和常量池，不可以增删属性和方法，不可以修改继承关系。
 * 2. 已初始化的类不会再次进行初始化（注意静态代码块）。
 * 3. 热更的方法，只有再次进入时才会生效。
 * 4. 被内联的方法可能提示热更新成功，却永远得不到执行（注意热点代码）。
 * 5. class对象引用不会改变，即不需要向其它热更新方式那样迁移数据（这是很大的优势）。
 * 6. 不可增删lambda表达式，不可以增删方法引用。
 * 7. 内部类和外部类必须一起热更新。
 *
 * <h3>违背直觉的情况</h3>
 * 1. lambda表达式：如果lambda表达式捕获的变量变更，将无法热更（因为编译时会生成特殊的粘合类，粘合类的成员属性会变更）。
 * 2. 内部类：如果需要访问另一个类的private字段，将无法热更（因为编译时会为其生成特殊的桥接方法，新增了静态方法）。
 * 3. switch：大型的switch语句无法热更（大型switch语句建议使用map进行映射）。
 *
 * <h3>奇巧淫技</h3>
 * 1. 每个manager额外定义一个通用方法 {@code Object execute(String cmd, Object params)}，当需要某个manager的功能和属性时，可以迂回救国。
 * 2. 每个manager额外定义一个黑板，比如就一个Map，这样当需要新增属性时，可以添加到map中。加上上一条，最好有个manager基类？
 * 3. 每个玩家额外定义两个黑板，比如两个map，一个存库，一个不存库。这样当需要在玩家身上新增属性时，可以存储到map中。
 * 4. 玩家与服务器之间可以预留几条通用协议，用于救急。
 * 5. 内部类的属性尽量声明为包级（默认权限），尽量少使用private，或提供getter/setter方法。
 *
 * <h3>使用方式</h3>
 * 1. 由于代理必须以jar包形式存在，因此文件检测，执行更新等逻辑，请写在自己的业务逻辑包中，不要写在这里，方便扩展。
 * 2. 热更新时不要一次更新太多类，否则可能导致停顿时间过长。
 * 3. 由于该API于IDE热更新是一套API，因此必须在本机上进行热更新测试，本机能通过，基本上运行环境也能通过。
 * 4. 每次启服后，需要进行一次热更流程，避免使用的是旧的class文件。
 * 5. 除非重启服务器，否则热更的代码不可删除。因此，只有在更版本的时候才可以删除热更代码，替换为正式的代码（这也是启服后必须执行一次热更的原因）。
 * 6. 热更只应该用于修改重大bug，不建议动不动就热更，平时要保证代码质量。
 *
 * <p>
 * debug下使用{@link #agentmain(String, Instrumentation)}比较方便，直接在ide中添加启动参数就可以。
 * 线上使用{@link #premain(String, Instrumentation)}方式比较方便。
 *
 * @author chauncy
 */
public class Agent {

    private static volatile Instrumentation instrumentation;

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    private static void setInstrumentation(Instrumentation instrumentation) {
        Agent.instrumentation = instrumentation;
    }


    /**
     * premain 方法在类加载之前执行，agentmain 方法在类加载之后执行
     *
     * @param agentArgs agent参数
     * @param inst      Instrumentation实例
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent] premain invoked. agentArgs: " + agentArgs);
        Agent.setInstrumentation(inst);
    }

    /**
     * {@inheritDoc}
     * premain 方法在类加载之前执行，agentmain 方法在类加载之后执行
     *
     * @param agentArgs agent参数
     * @param inst      Instrumentation实例
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent] agentmain invoked. agentArgs: " + agentArgs);
        Agent.setInstrumentation(inst);
    }

}
