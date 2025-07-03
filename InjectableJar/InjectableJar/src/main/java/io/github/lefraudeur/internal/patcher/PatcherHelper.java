package io.github.lefraudeur.internal.patcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// bunch of static methods called by c++ transformer
public class PatcherHelper
{
    private static Class<?>[] classesToTransform = null;
    private static Map<Class<?>, ClassModifier> classModifierMap = new HashMap<>();

    public static boolean init()
    {
        List<Class<?>> classes = new ArrayList<>();
        for (ClassModifier classModifier : io.github.lefraudeur.internal.patcher.Patcher.classModifiers)
        {
            try
            {
                Class<?> classModifierClass = PatcherHelper.class.getClassLoader().loadClass(classModifier.name.replace('/', '.'));
                classes.add(classModifierClass);
                classModifierMap.put(classModifierClass, classModifier);
            } catch (ClassNotFoundException e)
            {
                return false;
            }
        }
        classesToTransform = classes.toArray(new Class<?>[0]);
        return true;
    }

    public static Class<?>[] getClassesToTransform()
    {
        return classesToTransform;
    }

    public static ClassModifier getClassModifier(Class<?> classToModify)
    {
        return classModifierMap.get(classToModify);
    }
}
