package cn.chauncy.apt.processor;

import cn.chauncy.apt.utils.AptUtils;
import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

public abstract class MyAbstractProcessor extends AbstractProcessor {

    protected Types typeUtils;
    protected Elements elementUtils;
    protected Messager messager;
    protected Filer filer;
    protected AnnotationSpec processorAnnotationSpec;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.processorAnnotationSpec = AptUtils.newProcessorGeneratedAnnotation(getClass());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            ensureInitialized();
        } catch (Throwable e) {
            messager.printMessage(Diagnostic.Kind.ERROR, AptUtils.getStackTrace(e));
            return false;
        }

        try {
            return doProcess(annotations, roundEnv);
        } catch (Throwable e) {
            messager.printMessage(Diagnostic.Kind.ERROR, AptUtils.getStackTrace(e));
            return false;
        }
    }

    protected abstract void ensureInitialized();

    /** true表是注解已被认领，false表示未被认领，后续其他的处理器可以继续处理该注解*/
    protected abstract boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

}
