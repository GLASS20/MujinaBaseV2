package io.github.lefraudeur.internal.patcher;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

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
    protected final Method method;
    protected final org.objectweb.asm.Type[] arguments;
    protected final org.objectweb.asm.Type returnType;

    public MethodModifier(Type type, MethodModifierInfo info)
    {
        this.type = type;
        this.info = info;
        this.method = new Method(info.methodName, info.methodDescriptor);
        this.arguments = method.getArgumentTypes();
        this.returnType = method.getReturnType();
    }

    public abstract MethodVisitor getMethodVisitor(MethodVisitor forwardTo, int access, String name, String descriptor);

    protected int getMinAvailableIndex()
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

    protected void pushParametersAndCallEventHandler(MethodVisitor mv)
    {
        if (!info.isStatic)
            mv.visitVarInsn(Opcodes.ALOAD, 0);

        for (int i = (info.isStatic ? 0 : 1), a = 0; a < arguments.length; ++i, ++a)
        {
            switch (arguments[a].getSort())
            {
                case org.objectweb.asm.Type.BOOLEAN:
                case org.objectweb.asm.Type.CHAR:
                case org.objectweb.asm.Type.BYTE:
                case org.objectweb.asm.Type.SHORT:
                case org.objectweb.asm.Type.INT:
                    mv.visitVarInsn(Opcodes.ILOAD, i);
                    break;
                case org.objectweb.asm.Type.FLOAT:
                    mv.visitVarInsn(Opcodes.FLOAD, i);
                    break;
                case org.objectweb.asm.Type.LONG:
                    mv.visitVarInsn(Opcodes.LLOAD, i);
                    ++i;
                    break;
                case org.objectweb.asm.Type.DOUBLE:
                    mv.visitVarInsn(Opcodes.DLOAD, i);
                    ++i;
                    break;
                case org.objectweb.asm.Type.ARRAY:
                case org.objectweb.asm.Type.OBJECT:
                    mv.visitVarInsn(Opcodes.ALOAD, i);
                    break;
                default:
                    throw new RuntimeException("incorrect argument Type or not implemented");
            }
        }

        // call event handler
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, info.eventMethodClass, info.eventMethodName, info.eventMethodSignature, false);
    }

    protected void visitReturnBasedOnReturnType(MethodVisitor mv)
    {
        int returnTypeSort = returnType.getSort();
        // if cancel :
        switch (returnTypeSort)
        {
            case org.objectweb.asm.Type.BOOLEAN:
            case org.objectweb.asm.Type.CHAR:
            case org.objectweb.asm.Type.BYTE:
            case org.objectweb.asm.Type.SHORT:
            case org.objectweb.asm.Type.INT:
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case org.objectweb.asm.Type.FLOAT:
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case org.objectweb.asm.Type.LONG:
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case org.objectweb.asm.Type.DOUBLE:
                mv.visitInsn(Opcodes.DRETURN);
                break;
            case org.objectweb.asm.Type.ARRAY:
            case org.objectweb.asm.Type.OBJECT:
                mv.visitInsn(Opcodes.ARETURN);
                break;
            case org.objectweb.asm.Type.VOID:
                mv.visitInsn(Opcodes.RETURN);
                break;
            default:
                throw new RuntimeException("incorrect argument Type or not implemented");
        }
    }

    protected void popReturnValueBasedOnReturnType(MethodVisitor mv)
    {
        int returnTypeSort = returnType.getSort();
        if (returnTypeSort != org.objectweb.asm.Type.VOID)
        {
            if (returnTypeSort == org.objectweb.asm.Type.DOUBLE || returnTypeSort == org.objectweb.asm.Type.LONG)
                mv.visitInsn(Opcodes.POP2);
            else
                mv.visitInsn(Opcodes.POP);
        }
    }
}
