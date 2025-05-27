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
    requires owner;
    requires redisson;
    requires zookeeper;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.google.protobuf;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;
    requires com.google.guice;
    requires org.slf4j;
    requires com.baomidou.mybatis.plus.core;
    requires org.mybatis;

    exports cn.chauncy.utils.log to java.logging;
}