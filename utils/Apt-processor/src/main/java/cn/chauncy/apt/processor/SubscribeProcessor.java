package cn.chauncy.apt.processor;

import cn.chauncy.apt.utils.AptUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_17)
public class SubscribeProcessor extends MyAbstractProcessor {

    private static final String SUBSCRIBE_TYPE = "com.chauncy.utils.eventbus.Subscribe";
    private static final String EVENTBUS_TYPE = "com.chauncy.utils.eventbus.EventBus";
    private static final String GENERIC_EVENT_TYPE = "com.chauncy.utils.eventbus.GenericEvent";

    private TypeElement sbscribeTypeElement;
    private TypeElement eventbusTypeElement;
    private TypeElement genericEventTypeElement;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(SUBSCRIBE_TYPE);
    }

    @Override
    protected void ensureInitialized() {
        if (sbscribeTypeElement == null || eventbusTypeElement == null) {
            sbscribeTypeElement = elementUtils.getTypeElement(SUBSCRIBE_TYPE);
            eventbusTypeElement = elementUtils.getTypeElement(EVENTBUS_TYPE);
            genericEventTypeElement = elementUtils.getTypeElement(GENERIC_EVENT_TYPE);
        }
    }

    @Override
    public boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        Map<Element, ? extends List<? extends Element>> class2MethodMap = roundEnv.getElementsAnnotatedWith(sbscribeTypeElement).stream()
                .collect(Collectors.groupingBy(Element::getEnclosingElement));
        for (Map.Entry<Element, ? extends List<? extends Element>> entry : class2MethodMap.entrySet()) {
            Element element = entry.getKey();
            List<? extends Element> elements = entry.getValue();
            try {
                genProxyClass((TypeElement) element, elements);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, AptUtils.getStackTrace(e), element);
            }
        }
        return true;
    }

    private void genProxyClass(TypeElement typeElement, List<? extends Element> methodList) throws Exception{
        String className = typeElement.getSimpleName().toString() + "Register";
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(processorAnnotationSpec)
                .addMethod(genRegisterMethodSpec(typeElement, methodList));

        String packageName = typeElement.getEnclosingElement().toString();
        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
        javaFile.writeTo(filer);
    }

    private MethodSpec genRegisterMethodSpec(TypeElement typeElement, List<? extends Element> methodList) {
        MethodSpec.Builder register = MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get(eventbusTypeElement), "registry")
                .addParameter(ClassName.get(typeElement), "subscriber");

        for (Element methodElement : methodList) {
            ExecutableElement method = (ExecutableElement) methodElement;
            if (method.getModifiers().contains(Modifier.STATIC)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Subscribe method can't be static", methodElement);
                continue;
            }
            if (method.getModifiers().contains(Modifier.PRIVATE)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Subscribe method can't be private", methodElement);
                continue;
            }

            if (method.getParameters().size() != 1) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Subscribe method can't have more than one parameter", methodElement);
                continue;
            }

            VariableElement variableElement = method.getParameters().get(0);
            if (variableElement.asType().getKind() != TypeKind.DECLARED) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Subscribe method parameter type must be class or interface", methodElement);
                continue;
            }

            TypeMirror variableType = variableElement.asType();
            TypeMirror genericEventType = genericEventTypeElement.asType();
            if (typeUtils.isSubtype(variableType, genericEventType)) {
                TypeMirror erasedType = typeUtils.erasure(variableType);
                TypeMirror genericType = ((DeclaredType) variableType).getTypeArguments().get(0);
                register.addStatement("registry.register($T.class, $T.class, (event) -> subscriber.$N(($T)event))", erasedType, genericType, method.getSimpleName(), variableType);
            } else {
                register.addStatement("registry.register($T.class, null, (event) -> subscriber.$N(($T) event))", variableType, method.getSimpleName(), variableElement.asType());
            }
        }

        return register.build();
    }

}
