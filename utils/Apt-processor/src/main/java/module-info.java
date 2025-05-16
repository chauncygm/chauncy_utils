import cn.chauncy.apt.processor.AutoMapperProcessor;
import cn.chauncy.apt.processor.SubscribeProcessor;

import javax.annotation.processing.Processor;

module Apt.processor {
    requires com.google.auto.service;
    requires com.squareup.javapoet;
    requires java.compiler;


    provides Processor with SubscribeProcessor, AutoMapperProcessor;
}