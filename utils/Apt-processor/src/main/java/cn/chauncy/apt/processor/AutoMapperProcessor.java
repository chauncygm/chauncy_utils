package cn.chauncy.apt.processor;

import cn.chauncy.annotation.AutoMapper;
import cn.chauncy.annotation.Subscribe;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "cn.chauncy.annotation.AutoMapper"
})
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_17)
public class AutoMapperProcessor extends MyAbstractProcessor {


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        if (roundEnv.processingOver()) {
            return true;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "START PROCESS ==> AutoMapper");
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoMapper.class);
        for (Element element : elements) {
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            ClassName elementClassName = ClassName.get(packageName, className);
            mapperProcess(element, packageName, className, elementClassName);
        }

        return true;
    }

    private void mapperProcess(Element element, String packageName, String className, ClassName elementClassName) {
        AutoMapper autoMapper = element.getAnnotation(AutoMapper.class);

        String name = autoMapper.name();
        if (name == null || name.isBlank()) {
            name = className + "Mapper";
        }

        TypeMirror baseMapperTypeMirror = readValue(element, AutoMapper.class, "baseMapper");

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(name).addModifiers(Modifier.PUBLIC);
        if (baseMapperTypeMirror != null) {
            builder.addSuperinterface(ClassName.get(baseMapperTypeMirror));
        }

        JavaFile javaFile = JavaFile.builder(packageName, builder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readValue(Element element, Class<?> clazz, String key) {
        AnnotationMirror annotationMirror = getEventTypeAnnotationMirror(element, clazz);
        if (annotationMirror == null) {
            return null;
        }
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);
        return annotationValue == null ? null : (T) annotationValue.getValue();
    }

    private AnnotationMirror getEventTypeAnnotationMirror(Element element, Class<?> type) {
        String clazzName = type.getName();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            String annotationTypeName = annotationMirror.getAnnotationType().toString();
            if (annotationTypeName.equals(clazzName)) {
                return annotationMirror;
            }
        }
        return null;
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
