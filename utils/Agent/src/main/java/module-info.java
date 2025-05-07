module Agent {
    requires java.instrument;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;

    exports cn.chauncy.agent;
}