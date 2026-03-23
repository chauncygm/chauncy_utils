package reload;

import cn.chauncy.utils.SystemUtils;
import cn.chauncy.utils.reload.ClassReloader;
import com.sun.tools.attach.VirtualMachine;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ReloadTest {

    private static final String AGENT_PATH = "../res/lib/Agent-1.0.jar";

    /**
     * 测试类重载，两种方式
     * 1.运行时设置jvm参数 -Djdk.attach.allowAttachSelf=true，调用下方的attachAgent()方法
     * 2.在启动时添加jvm参数 -javaagent:../res/lib/Agent-1.0.jar
     */
    @Test
    public void test() throws Exception {
        // 热更前：打印jvm信息
        System.out.println(SystemUtils.getJVMInfo());

        // 将需要热更类的class文件(一个类可能包含多个，如内部类,lambda等)拷贝到classes目录下
        ClassReloader reloader = new ClassReloader("../res", "./classes");
        reloader.reloadAllClass();

        // 热更后：打印jvm信息
        System.out.println(SystemUtils.getJVMInfo());
    }

    private void attachAgent() throws Exception {
        int pid = SystemUtils.getProcessId();
        VirtualMachine vm = VirtualMachine.attach(pid + "");
        vm.loadAgent(AGENT_PATH);
        System.out.println("Agent loaded, agent path:" + new File(AGENT_PATH).getAbsolutePath() + ", pid: " + pid);
    }

}
