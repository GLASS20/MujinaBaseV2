package io.github.lefraudeur.internal.patcher;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassModifier extends Modifier
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
        return null;
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
