package io.github.lefraudeur.internal.patcher;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Method;

public abstract class MethodModifier implements NewInstanceStringable
{
    public enum Type
    {
        ON_ENTRY,
        ON_RETURN
    }

    public static class MethodModifierInfo implements NewInstanceStringable
    {
        public final String methodName;
        public final String methodSignature;
        public final String eventMethodClass;
        public final String eventMethodName;
        public final String eventMethodSignature;
        public final boolean isStatic;

        public MethodModifierInfo(String methodName, String methodSignature,
                                  String eventMethodClass, String eventMethodName, String eventMethodSignature,
                                  boolean isStatic)
        {
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.eventMethodClass = eventMethodClass;
            this.eventMethodName = eventMethodName;
            this.eventMethodSignature = eventMethodSignature;
            this.isStatic = isStatic;
        }

        @Override
        public String getNewInstanceCode()
        {
            return String.format("new MethodModifierInfo(\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %b)",
                    methodName,
                    methodSignature,
                    eventMethodClass,
                    eventMethodName,
                    eventMethodSignature,
                    isStatic);
        }
    }

    public final Type type;
    public final MethodModifierInfo info;

    public MethodModifier(Type type, MethodModifierInfo info)
    {
        this.type = type;
        this.info = info;
    }

    public abstract MethodVisitor getMethodVisitor(MethodVisitor forwardTo, MethodVisitor finalVisitor, int access, String name, String descriptor);
}
