package io.github.lefraudeur.internal.patcher;

import io.github.lefraudeur.internal.Canceler;
import io.github.lefraudeur.internal.Thrower;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;


public class ReturnThrowMethodModifier extends MethodModifier
{

    public ReturnThrowMethodModifier(MethodModifierInfo info)
    {
        super(Type.ON_RETURN_THROW, info);
    }

    @Override
    public String getNewInstanceCode()
    {
        return String.format("new ReturnThrowMethodModifier(%s)", this.info.getNewInstanceCode());
    }

    @Override
    public MethodVisitor getMethodVisitor(MethodVisitor forwardTo, int access, String name, String descriptor)
    {
        return new AdviceAdapter(Opcodes.ASM9, forwardTo, access, name, descriptor)
        {
            private final Method method = new Method(info.methodName, info.methodDescriptor);
            private final org.objectweb.asm.Type[] arguments = method.getArgumentTypes();
            private int availableVarIndex = getMinAvailableIndex(arguments);

            @Override
            public void visitVarInsn(int opcode, int varIndex)
            {
                if (isStoreOpcode(opcode))
                {
                    int newAvailableVarIndex = varIndex + 1;
                    if (opcode == Opcodes.DSTORE || opcode == Opcodes.LSTORE)
                        newAvailableVarIndex++;
                    if (newAvailableVarIndex > availableVarIndex) availableVarIndex = newAvailableVarIndex;
                }
                super.visitVarInsn(opcode, varIndex);
            }

            @Override
            protected void onMethodExit(int opcode)
            {
                /*
                String ThrowerClassName = Thrower.class.getName().replace('.', '/');
                mv.visitTypeInsn(Opcodes.NEW, ThrowerClassName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.DUP);
                int throwerVarIndex = availableVarIndex;
                availableVarIndex++;
                mv.visitVarInsn(Opcodes.ASTORE, throwerVarIndex);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ThrowerClassName, "<init>", "()V", false);


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
                 */
            }

            private boolean isStoreOpcode(int opcode)
            {
                final int[] storeOpcodes = new int[]{Opcodes.ISTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.LSTORE, Opcodes.ASTORE};
                for (int op : storeOpcodes)
                    if (op == opcode) return true;
                return false;
            }
        };
    }
}
