package reload;

import cn.chauncy.utils.SystemUtils;
import cn.chauncy.utils.reload.ClassReloader;
import com.sun.tools.attach.VirtualMachine;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ReloadTest {

    private static final String AGENT_PATH = "../res/lib/Agent-1.0.jar";

    /**
     * 测试类重载
     * 需要在运行时设置jvm参数 -Djdk.attach.allowAttachSelf=true
     */
    @Test
    public void test() throws Exception {
        int pid = SystemUtils.getProcessId();
        System.out.println("pid: " + pid);
        System.out.println(new File(".").getAbsolutePath());
        VirtualMachine vm = VirtualMachine.attach(pid + "");
        vm.loadAgent(AGENT_PATH);

        // 热更前：打印jvm信息
        System.out.println(SystemUtils.getJVMInfo());

        ClassReloader reloader = new ClassReloader("../res", "./classes");
        reloader.reloadAllClass();

        // 热更后：打印jvm信息
        System.out.println(SystemUtils.getJVMInfo());
    }

}
