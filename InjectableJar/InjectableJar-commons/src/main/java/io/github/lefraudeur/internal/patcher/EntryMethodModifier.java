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
                if (USE_REFLECTION)
                {
                    reflectionImplementation(mv);
                    return;
                }

                // new Canceler()
                String CancelerClassName = Canceler.class.getName().replace('.', '/');
                mv.visitTypeInsn(Opcodes.NEW, CancelerClassName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.DUP);
                int cancelerVarIndex = availableVarIndex;
                availableVarIndex++;
                mv.visitVarInsn(Opcodes.ASTORE, cancelerVarIndex);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CancelerClassName, "<init>", "()V", false);

                pushParametersAndCallEventHandler(mv);

                // cancel if required to
                Label pop = new Label();

                mv.visitVarInsn(Opcodes.ALOAD, cancelerVarIndex);
                mv.visitFieldInsn(Opcodes.GETFIELD, CancelerClassName, "cancel", "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, pop);
                // if cancel
                visitReturnBasedOnReturnType(mv);
                // endif cancel

                mv.visitLabel(pop);
                // if not cancel
                // discard return value and continue method execution
                popReturnValueBasedOnReturnType(mv);
            }
        };
    }

    private void reflectionImplementation(MethodVisitor mv)
    {

        return;
    }
}
