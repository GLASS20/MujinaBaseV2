package io.github.lefraudeur.internal.patcher;


import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassModifier implements NewInstanceStringable
{
    public final String name;
    private final List<MethodModifier> modifiers;

    public ClassModifier(String name)
    {
        this.name = name;
        modifiers = new ArrayList<>();
    }

    public ClassModifier(String name, MethodModifier[] modifiers)
    {
        this.name = name;
        this.modifiers = Arrays.asList(modifiers);
    }

    public byte[] patch(byte[] originalBytes)
    {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassReader classReader = new ClassReader(originalBytes);
        classReader.accept(new ClassVisitor(Opcodes.ASM9, classWriter)
        {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
            {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                MethodVisitor finalVisitor = visitor;
                for (MethodModifier modifier : modifiers)
                {
                    if
                    (!(
                        modifier.info.methodName.equals(name)
                        && modifier.info.methodSignature.equals(descriptor)
                        && (modifier.info.isStatic == ((access & Opcodes.ACC_STATIC) != 0))
                    ))
                        continue;
                    visitor = modifier.getMethodVisitor(visitor, finalVisitor, access, name, descriptor);
                }
                return visitor;
            }
        }, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }

    public void addModifier(MethodModifier modifier)
    {
        modifiers.add(modifier);
    }

    @Override
    public String getNewInstanceCode()
    {
        String str = "new ClassModifier(\"%s\", new MethodModifier[]{%s})";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < modifiers.size(); ++i)
        {
            MethodModifier modifier = modifiers.get(i);
            builder.append(modifier.getNewInstanceCode());
            if (i == modifiers.size() -1) break;
            builder.append(", ");
        }
        return String.format(str, name, builder);
    }
}
