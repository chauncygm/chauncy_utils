package cn.chauncy.apt.processor;

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
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_17)
public class AutoMapperProcessor extends MyAbstractProcessor {

    private static final String AUTO_MAPPER_TYPE = "cn.chauncy.utils.mapper.AutoMapper";

    private TypeElement autoMapperTypeElement;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    protected void ensureInitialized() {
        if (autoMapperTypeElement == null) {
            autoMapperTypeElement = elementUtils.getTypeElement(AUTO_MAPPER_TYPE);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(AUTO_MAPPER_TYPE);
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
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(autoMapperTypeElement);
        for (Element element : elements) {
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            ClassName elementClassName = ClassName.get(packageName, className);
            mapperProcess(element, packageName, className, elementClassName);
        }
        return true;
    }

    private void mapperProcess(Element element, String packageName, String className, ClassName elementClassName) {

        String className2 = className + "Mapper";
        TypeMirror baseMapperTypeMirror = readValue(element, autoMapperTypeElement, "baseMapper");

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className2).addModifiers(Modifier.PUBLIC);
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
    private <T> T readValue(Element element, TypeElement type, String key) {
        AnnotationMirror annotationMirror = getEventTypeAnnotationMirror(element, type);
        if (annotationMirror == null) {
            return null;
        }
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);
        return annotationValue == null ? null : (T) annotationValue.getValue();
    }

    private AnnotationMirror getEventTypeAnnotationMirror(Element element, TypeElement type) {
        String clazzName = type.getQualifiedName().toString();
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
