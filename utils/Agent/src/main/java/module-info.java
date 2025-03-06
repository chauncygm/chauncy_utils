module cn.chauncy.agent {
    requires java.instrument;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;

    exports cn.chauncy.agent;
    opens cn.chauncy.agent;
}