package io.github.lefraudeur.internal.patcher;

import org.objectweb.asm.MethodVisitor;

public class ReturnMethodModifier extends MethodModifier
{

    public ReturnMethodModifier(MethodModifierInfo info)
    {
        super(Type.ON_RETURN, info);
    }

    @Override
    public String getNewInstanceCode()
    {
        return String.format("new ReturnMethodModifier(%s)", this.info.getNewInstanceCode());
    }

    @Override
    public MethodVisitor getMethodVisitor(MethodVisitor forwardTo, MethodVisitor finalVisitor, int access, String name, String descriptor)
    {
        return null;
    }
}
