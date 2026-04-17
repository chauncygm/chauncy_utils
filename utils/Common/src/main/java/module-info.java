module Common {
    // 热更支持
    requires Agent;
    requires jdk.attach;

    // jdk
    requires java.compiler;
    requires java.instrument;
    requires java.logging;
    requires java.management;

    // guava
    requires com.google.common;
    requires com.google.protobuf;

    // jackson
    requires com.fasterxml.jackson.databind;

    // netty
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;

    // commons
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.apache.commons.lang3;

    // disruptor
    requires com.lmax.disruptor;

    // fastutil/jctools
    requires it.unimi.dsi.fastutil;
    requires org.jctools.core;

    // lombok
    requires static lombok;

    requires dubbo;
    requires jol.core;
    requires jsr305;
    requires owner;
    requires redisson;
    requires zookeeper;
    requires java.sql;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;
    requires com.google.guice;
    requires com.baomidou.mybatis.plus.core;
    requires org.mybatis;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires aopalliance;

    exports cn.chauncy.utils.log to java.logging;
    exports cn.chauncy.utils.rpc.service;
    exports cn.chauncy.behavior_tree to com.fasterxml.jackson.databind;

    opens cn.chauncy.utils.rpc.service;
    opens cn.chauncy.utils.reload to jdk.attach;
    opens cn.chauncy.utils.reload.tool to jdk.attach;
    opens cn.chauncy.behavior_tree to com.fasterxml.jackson.databind;
    opens cn.chauncy.behavior_tree.branch to com.fasterxml.jackson.databind;
    opens cn.chauncy.behavior_tree.leaf to com.fasterxml.jackson.databind;
    opens cn.chauncy.behavior_tree.decorate to com.fasterxml.jackson.databind;

    uses com.sun.tools.attach.spi.AttachProvider;
}