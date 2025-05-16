package cn.chauncy.apt.utils;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.processing.Generated;
import java.io.PrintWriter;
import java.io.StringWriter;

public class AptUtils {

    private AptUtils() {
    }

    public static AnnotationSpec newProcessorGeneratedAnnotation(Class<?> processorClass) {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", processorClass.getCanonicalName())
                .build();
    }


    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
