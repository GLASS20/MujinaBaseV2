package io.github.lefraudeur.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventClassLoader extends ClassLoader
{
    private final ClassLoader newClassLoader;

    public EventClassLoader(ClassLoader parent, ClassLoader newClassloader)
    {
        super(parent);
        this.newClassLoader = newClassloader;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        try
        {
            return super.loadClass(name, resolve);
        }
        catch (ClassNotFoundException ignored)
        {
        }

        Class<?> c = extraLoadClass(name);
        if (resolve)
            resolveClass(c);

        return c;
    }

    private Class<?> extraLoadClass(String name) throws ClassNotFoundException
    {
        Method findLoadedClassMethod = getClassLoaderMethod("findLoadedClass");
        Method findClassMethod = getClassLoaderMethod("findClass");

        if (findLoadedClassMethod == null || findClassMethod == null)
            throw new ClassNotFoundException("could not get ClassLoader methods through reflection");

        try
        {
            Object found = findLoadedClassMethod.invoke(newClassLoader, name);
            if (found != null)
                return (Class<?>)found;
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }

        try
        {
            return (Class<?>)findClassMethod.invoke(newClassLoader, name);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException ignored)
        {
        }

        throw new ClassNotFoundException("tried everything");
    }

    private Method getClassLoaderMethod(String name)
    {
        for (Method m : ClassLoader.class.getDeclaredMethods())
        {
            if (m.getName().equals(name))
            {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }
}
