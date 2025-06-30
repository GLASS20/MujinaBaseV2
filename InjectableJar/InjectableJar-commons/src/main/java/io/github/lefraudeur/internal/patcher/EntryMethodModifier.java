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
                mv.visitTypeInsn(Opcodes.NEW, CancelerClassName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CancelerClassName, "<init>", "()V", false);


                // push method this + method parameters on stack
                if (!info.isStatic)
                    mv.visitVarInsn(Opcodes.ALOAD, 0);

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
                if (method.getReturnType().getSort() != org.objectweb.asm.Type.VOID)
                    mv.visitInsn(Opcodes.POP);
            }
        };
    }
}
