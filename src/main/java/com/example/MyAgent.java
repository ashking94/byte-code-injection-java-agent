package com.example;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class MyAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new LoggingClassFileTransformer());
    }

    static class MyTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws IllegalClassFormatException {

            // Perform bytecode manipulation here using ASM or Byte Buddy
            System.out.println("Starting manipulation ==> " + className);
            return classfileBuffer; // Return the modified bytecode
        }
    }

    static class LoggingClassFileTransformer implements ClassFileTransformer {

        private static final String CLASS_TO_TRANSFORM = "com.example.App";
        private static final String METHOD_TO_LOG = "sayHelloAgain";

        @Override
        public byte[] transform(
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer) {

            byte[] transformedClass = classfileBuffer;

            if (className.equals(CLASS_TO_TRANSFORM.replace(".", "/"))) {
                try {
                    ClassPool classPool = ClassPool.getDefault();
                    CtClass ctClass = classPool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

                    CtMethod[] methods = ctClass.getDeclaredMethods();
                    for (CtMethod method : methods) {
                        if (method.getName().equals(METHOD_TO_LOG)) {
                            // Add import statements to the class's classPool
                            classPool.importPackage("java.time.LocalDateTime");

                            // Add Java code snippet at the start of the method
                            String codeSnippet = "System.out.println(\"nanoTime=\" + System.nanoTime() + \" currentTime=\" + LocalDateTime.now());";
                            method.insertBefore(codeSnippet);
                            method.insertAfter(codeSnippet);
                        }
                    }

                    transformedClass = ctClass.toBytecode();
                    ctClass.detach();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return transformedClass;
        }
    }
}



