package io.github.lefraudeur.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class MemoryJarClassLoader extends ClassLoader
{
    private final byte[] jarBytes;

    public MemoryJarClassLoader(byte[] jarBytes)
    {
        this.jarBytes = jarBytes;
    }

    public MemoryJarClassLoader(byte[] jarBytes, ClassLoader parent)
    {
        super(parent);
        this.jarBytes = jarBytes;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        // make sure we use our own asm library, not the one already in game
        if (name.startsWith("org.objectweb.asm"))
        {
            Class<?> alreadyLoaded = findLoadedClass(name);
            if (alreadyLoaded != null)
                return alreadyLoaded;

            return findClass(name);
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        String path = name.replace('.', '/');
        path += ".class";
        byte[] classBytes = extract(path);
        if (classBytes == null)
            throw new ClassNotFoundException(name);
        return defineClass(name, classBytes, 0, classBytes.length);
    }

    @Override
    public InputStream getResourceAsStream(String name)
    {
        InputStream stream = super.getResourceAsStream(name);
        if (stream != null)
            return stream;
        String path = "resources/" + name.replace('.', '/');
        byte[] extracted = extract(name);
        if (extracted == null)
            return null;
        return new ByteArrayInputStream(extracted);
    }

    private byte[] extract(String name)
    {
        byte[] result = null;
        try (JarInputStream inputStream = new JarInputStream(new ByteArrayInputStream(jarBytes)))
        {
            for (JarEntry entry = inputStream.getNextJarEntry(); entry != null; entry = inputStream.getNextJarEntry())
            {
                if (entry.isDirectory() || !entry.getName().equals(name))
                {
                    inputStream.closeEntry();
                    continue;
                }

                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                byte[] buff = new byte[4096];
                int read = 0;
                while ((read = inputStream.read(buff)) != -1)
                    arrayOutputStream.write(buff, 0, read);

                result = arrayOutputStream.toByteArray();

                inputStream.closeEntry();
                break;
            }
        }
        catch (IOException e)
        {
            return null;
        }
        return result;
    }
}
