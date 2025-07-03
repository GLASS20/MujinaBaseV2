package io.github.lefraudeur.internal.patcher;

import io.github.lefraudeur.internal.Canceler;
import org.objectweb.asm.Label;
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
    public MethodVisitor getMethodVisitor(MethodVisitor forwardTo, int access, String name, String descriptor)
    {
        return new AdviceAdapter(Opcodes.ASM9, forwardTo, access, name, descriptor)
        {
            @Override
            protected void onMethodEnter()
            {
                Method method = new Method(info.methodName, info.methodDescriptor);
                org.objectweb.asm.Type[] arguments = method.getArgumentTypes();

                // new Canceler()
                String CancelerClassName = Canceler.class.getName().replace('.', '/');
                mv.visitTypeInsn(Opcodes.NEW, CancelerClassName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.DUP);
                int cancelerVarIndex = getMinAvailableIndex(arguments);
                mv.visitVarInsn(Opcodes.ASTORE, cancelerVarIndex);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CancelerClassName, "<init>", "()V", false);


                // push method this + method parameters on stack
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

                // cancel if required to
                Label pop = new Label();

                mv.visitVarInsn(Opcodes.ALOAD, cancelerVarIndex);
                mv.visitFieldInsn(Opcodes.GETFIELD, CancelerClassName, "cancel", "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, pop);

                int returnTypeSort = method.getReturnType().getSort();
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
                // endif cancel

                mv.visitLabel(pop);
                // if not cancel
                if (returnTypeSort != org.objectweb.asm.Type.VOID)
                {
                    if (returnTypeSort == org.objectweb.asm.Type.DOUBLE || returnTypeSort == org.objectweb.asm.Type.LONG)
                        mv.visitInsn(Opcodes.POP2);
                    else
                        mv.visitInsn(Opcodes.POP);
                }
            }
        };
    }
}
