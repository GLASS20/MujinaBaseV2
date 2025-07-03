package io.github.lefraudeur.internal.patcher;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public abstract class MethodModifier implements NewInstanceStringable
{
    public enum Type
    {
        ON_ENTRY,
        ON_RETURN_THROW
    }

    public static class MethodModifierInfo implements NewInstanceStringable
    {
        public final String methodName;
        public final String methodDescriptor;
        public final String eventMethodClass;
        public final String eventMethodName;
        public final String eventMethodSignature;
        public final boolean isStatic;

        public MethodModifierInfo(String methodName, String methodDescriptor,
                                  String eventMethodClass, String eventMethodName, String eventMethodSignature,
                                  boolean isStatic)
        {
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
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
                    methodDescriptor,
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

    public abstract MethodVisitor getMethodVisitor(MethodVisitor forwardTo, int access, String name, String descriptor);

    protected int getMinAvailableIndex(org.objectweb.asm.Type[] arguments)
    {
        int result = 0;
        for (org.objectweb.asm.Type arg : arguments)
        {
            result++;
            if (arg.getSort() == org.objectweb.asm.Type.DOUBLE || arg.getSort() == org.objectweb.asm.Type.LONG)
                result++;
        }
        if (!info.isStatic)
            result++;
        return result;
    }
}
