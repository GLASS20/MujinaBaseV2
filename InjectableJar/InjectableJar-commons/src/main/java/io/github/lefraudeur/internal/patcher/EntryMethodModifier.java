package io.github.lefraudeur.internal.patcher;

import io.github.lefraudeur.internal.Canceler;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class EntryMethodModifier extends MethodModifier
{
    public EntryMethodModifier(MethodModifierInfo info)
    {
        super(Type.ON_ENTRY, info);
    }


    @Override
    public String getNewInstanceCode()
    {
        return String.format("new EntryMethodModifier(%s)", this.info.getNewInstanceCode());
    }

    @Override
    public MethodVisitor getMethodVisitor(MethodVisitor forwardTo, MethodVisitor finalVisitor, int access, String name, String descriptor)
    {
        return new AdviceAdapter(Opcodes.ASM9, forwardTo, access, name, descriptor)
        {
            @Override
            protected void onMethodEnter()
            {
                // new Canceler()
                String CancelerClassName = Canceler.class.getName().replace('.', '/');
                finalVisitor.visitTypeInsn(Opcodes.NEW, CancelerClassName);
                finalVisitor.visitTypeInsn(Opcodes.DUP, CancelerClassName);
                finalVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, CancelerClassName, "<init>", "()V", false);


                // push method this + method parameters on stack
                if (!info.isStatic)
                    finalVisitor.visitVarInsn(Opcodes.ALOAD, 0);

                Method method = new Method(info.methodName, info.methodSignature);
                org.objectweb.asm.Type[] arguments = method.getArgumentTypes();

                for (int i = (info.isStatic ? 0 : 1), a = 0; a < arguments.length; ++i, ++a)
                {
                    switch (arguments[a].getSort())
                    {
                        case org.objectweb.asm.Type.BOOLEAN:
                        case org.objectweb.asm.Type.CHAR:
                        case org.objectweb.asm.Type.BYTE:
                        case org.objectweb.asm.Type.SHORT:
                        case org.objectweb.asm.Type.INT:
                            finalVisitor.visitVarInsn(Opcodes.ILOAD, i);
                            break;
                        case org.objectweb.asm.Type.FLOAT:
                            finalVisitor.visitVarInsn(Opcodes.FLOAD, i);
                            break;
                        case org.objectweb.asm.Type.LONG:
                            finalVisitor.visitVarInsn(Opcodes.LLOAD, i);
                            ++i;
                            break;
                        case org.objectweb.asm.Type.DOUBLE:
                            finalVisitor.visitVarInsn(Opcodes.DLOAD, i);
                            ++i;
                            break;
                        case org.objectweb.asm.Type.ARRAY:
                        case org.objectweb.asm.Type.OBJECT:
                            finalVisitor.visitVarInsn(Opcodes.ALOAD, i);
                            break;
                        default:
                            throw new RuntimeException("incorrect argument Type or not implemented");
                    }
                }

                // call event handler
                finalVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, info.eventMethodClass, info.eventMethodName, info.eventMethodSignature, false);
                if (method.getReturnType().getSort() != org.objectweb.asm.Type.VOID)
                    finalVisitor.visitInsn(Opcodes.POP);
            }
        };
    }
}
