module Common {
    requires Agent;
    requires com.google.common;
    requires dubbo;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;
    requires it.unimi.dsi.fastutil;
    requires java.compiler;
    requires java.instrument;
    requires java.logging;
    requires java.management;
    requires jol.core;
    requires jsr305;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.checkerframework.checker.qual;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;
    requires owner;
    requires redisson;
    requires zookeeper;
    requires java.sql;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;

    exports com.chauncy.utils.log to java.logging;
}