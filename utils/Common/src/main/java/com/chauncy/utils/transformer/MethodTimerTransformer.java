package com.chauncy.utils.transformer;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class MethodTimerTransformer implements ClassFileTransformer {

    private static final Set<String> EXCLUDED = Set.of(
            "java/", "sun/", "jdk/", "org/objectweb/asm/"
    );

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        try {
            if (className == null || EXCLUDED.stream().anyMatch(className::startsWith)) {
                System.out.println("[Agent] skip class: " + className);
                return null;
            }

            System.out.println("[Agent] transform class: " + className);
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    // 新增native方法检查
                    if ((access & ACC_NATIVE) != 0) return super.visitMethod(access, name, desc, signature, exceptions);

                    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                    return new LocalVariablesSorter(ASM9, access, desc, mv) {
                        private int timeVar;

                        @Override
                        public void visitCode() {
                            super.visitCode();
                            timeVar = newLocal(Type.LONG_TYPE);
                            visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                            visitVarInsn(LSTORE, timeVar);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                                // 使用timeVar变量索引
                                visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                                visitVarInsn(LLOAD, timeVar);
                                visitInsn(LSUB);

                                // 修正后的参数构造逻辑
                                visitLdcInsn("Method %s.%s executed in %d ns");
                                visitInsn(ICONST_3);
                                visitTypeInsn(ANEWARRAY, "java/lang/Object");

                                // 填充类名
                                visitInsn(DUP);
                                visitInsn(ICONST_0);
                                visitVarInsn(ALOAD, 0);
                                visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                                visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
                                visitInsn(AASTORE);

                                // 填充方法名
                                visitInsn(DUP);
                                visitInsn(ICONST_1);
                                visitLdcInsn(name);
                                visitInsn(AASTORE);

                                // 填充耗时值
                                visitInsn(DUP);
                                visitInsn(ICONST_2);
                                visitInsn(DUP2_X1);
                                visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                                visitInsn(AASTORE);

                                // 调用String.format
                                visitMethodInsn(INVOKESTATIC, "java/lang/String", "format",
                                        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
                                visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                                visitInsn(SWAP);
                                visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
            };
            reader.accept(cv, ClassReader.EXPAND_FRAMES);

            // 二次验证字节码
            validateBytecode(writer.toByteArray());
            return writer.toByteArray();
        } catch (Exception e) {
            System.out.println("[Agent] transform class error: " + className + ", cause: " + e.getMessage());
            return null;
        }
    }


    private void validateBytecode(byte[] code) {
        try {
            ClassReader cr = new ClassReader(code);
            ClassWriter cw = new ClassWriter(0);
            CheckClassAdapter cv = new CheckClassAdapter(cw, true);
            cr.accept(cv,  ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid bytecode generated", e);
        }
    }

    private static class SafeClassWriter extends ClassWriter {
        public SafeClassWriter(ClassReader reader) {
            super(reader, COMPUTE_FRAMES);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            return "java/lang/Object";
        }
    }
}
