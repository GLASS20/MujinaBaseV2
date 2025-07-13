# MujinaBaseV2

An injectable java cheat base for minecraft with a cool event system, loaded with C++, that can remap to work on vanilla and forge

The base is for 1.7.10 minecraft, however it can be adapted to work on any minecraft version. \
It works only on windows at the moment, however it could be modified to work on linux fairly easily \
(replace building scripts, and add jvm.so).

# Building

The project is meant to be fully portable, so it should work on any windows environment by running [build_srg.bat](build_srg.bat) (for forge) or [build_vanilla.bat](build_vanilla.bat) (for vanilla), without manually installing dependencies or building tools.\
However the maven project relies on multiple online repositories, which could break the build if they were to go offline.

Since you don't build using your IDE, make sure you save all the files you edited with your IDE first.

# Using the base

The actual java cheat that's beeing injected is the maven project in the folder [InjectableJar/InjectableJar](InjectableJar/InjectableJar).\
It is the only part of the project you should need to modify most of the time as a user, and where you would code your modules.\
In this project, Minecraft renamed jar (official client jar but rempped to human readable mcp names) is included as a dependency, and you can refer Minecraft classes in your code.\
It can't be built alone, as it depends on the other projects InjectableJar-commons and InjectableJar-processor, so make sure to open your IDE in the parent maven project \
Make sure to configure it to use the right jdk version (java 8)

## Event system
The real hassle when making an injectable cheat in java, is not really adding the jar to the classpath, it usually takes only one line of code, \
but actually setting up "events", meaning adding instructions to a game's function so that it calls one of the method in your cheat jar. \
Otherwise you just have useless classes in the jvm that are never used. \
The @EventHandler annotation allows you to do that. \
Be aware that the @EventHandler annotation is first checked by the annotation processor for incoherent target method and event handler signature, and will fail the compilation in such cases. \
It is purposefully strict to avoid having surprises at runtime.

There are some examples in [io/github/lefraudeur/TestClass.java](io/github/lefraudeur/TestClass.java)

### Add a new event handler
#### On entry event handler
This can be used to add a call to your event handler at the beginning of a method. \
To declare an event handler you have to define a public static method and annotate with @EventHandler :
```
    @EventHandler(type=ON_ENTRY,
            targetClass = "net/minecraft/client/entity/EntityClientPlayerMP",
            targetMethodName = "sendChatMessage",
            targetMethodDescriptor = "(Ljava/lang/String;)V",
            targetMethodIsStatic = false)
    public static void sendChatMessage(Canceler canceler, EntityClientPlayerMP player, String message)
    {
        System.out.println("sendChatMessage on entry succeeded");
    }
```
The return type must be the same as the target method's one \
The first parameter must be of type Canceler. \
If the target method is static, the second parameter type must be targetClass (it will hold this object). \
The ramaining parameters must be the same as the target method ones.

You can set `canceler.cancel = true`, to immediately return and cancel the execution of the method, in this case the return value will be the one returned by the event handler.\
If not canceled, the return value of the event handler is ignored.

targetMethodDescriptor : as described in [Java Virtual Machine Specification 4.3.3](https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-4.html#jvms-4.3.3)

### On return/throw event handler
This can be used to add a call to your event handler before any XRETURN / ATHROW instruction of a method. \
To declare an event handler you have to define a public static method and annotate with @EventHandler :
```
    @EventHandler(type=ON_RETURN_THROW,
            targetClass = "net/minecraft/client/ClientBrandRetriever",
            targetMethodName = "getClientModName",
            targetMethodDescriptor = "()Ljava/lang/String;",
            targetMethodIsStatic = true)
    public static String getClientModName(String returnValue, Thrower thrower)
    {
        return returnValue + " (Mujina Boosted)";
    }
```
The return type must be the same as the target method's one, the value returned by the event handler will be the value returned by the target method.\
The first parameter must be of the same type as the target method's one, it will hold the original return value.\
If the event was triggered because of an ATHROW instruction, then `thrower.thrown` will hold the thrown value, and you can use that field to override it.


# How do I call the original unmodified method ?
At the moment, there is no way to call the original method that won't trigger the event. \
So do not call the modified method from its event handler, otherwise you will end up in an infinite loop. \
The possible workarounds would be :
- Make a boolean that would tell your handler to immediatly return without doing anything (not reliable)
- Reimplement the original method yourself (takes a lot of time, and will need to use reflection for private fields/methods)


# How can I access private fields/methods ?
The project does not include any easy way to access private fields/methods of the game, \
You will have to use java reflection api to do that \
However be aware that the strings you will use to make your reflection calls won't be remapped automatically. \
It is planned to include a way to remap these strings automatically in the future.


# Working Principle
## Building process detailed
- 

## What happens on inject


# How do I adapt the project to work with another minecraft version ?
I've tried my best to explain the structure of this project in the previous sections, make sure to read them carefully, as well as check out the external references.

Some hints about what you might need to modify:
- [InjectableJar/InjectableJar/remapper/1.7.10.tiny](InjectableJar/InjectableJar/remapper/1.7.10.tiny), to whatever mappings you wish to apply
- [InjectableJar/InjectableJar/remapper/client.jar](InjectableJar/InjectableJar/remapper/client.jar) (original vanilla mineecraft client) from [minecraft version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json)
- [InjectableJar/InjectableJar/remapper/client_named.jar](InjectableJar/InjectableJar/remapper/client_named.jar) / [client_srg.jar](InjectableJar/InjectableJar/remapper/client_srg.jar), [client.jar](InjectableJar/InjectableJar/remapper/client.jar) remapped using tiny remapper to understandable names for coding (named), and to other intermediary names (srg in this case)
- minecraft jar (client_named.jar) used as a dependency to code InjectableJar.jar, defined in InjectableJar/pom.xml :
    ```xml
    <dependency>
        <groupId>minecraft.client</groupId>
        <artifactId>named</artifactId>
        <version>1.7.10</version>
    </dependency>
    ```
    So you will have to install the client_named.jar to [local_maven_repo](InjectableJar\InjectableJar\local_maven_repo) using :
    ```
    mvn org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file -Dfile=../client_named.jar -DgroupId=your.groupId -DartifactId=your-artifactId -Dversion=version -Dpackaging=jar -DlocalRepositoryPath=local_maven_repo
    ```
- minecraft dependencies, listed in the corresponding [minecraft version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), and that are added to [InjectableJar/pom.xml](InjectableJar/pom.xml) dependencies
- [InjectableJar/pom.xml](InjectableJar/pom.xml) properties : remapper.mappingFilePath, remapper.sourceNamespace, remapper.destinationNamespace which can be overridden in the `mvn package` command using `-Dremapper.sourceNamespace=value`

And rarely, when you switch java version:
- jdk (jdk8u442-b06) and its reference in env.bat
- InjectableJar/pom.xml property : maven.compiler.release (java version has to be lower or equal to the one of the jvm you inject to)
- asm library version and api version (Opcodes.ASMX)

# How do I add a dependency to my jar
Most of the time you should edit [InjectableJar/InjectableJar/pom.xml](InjectableJar/InjectableJar/pom.xml) `<dependencies>` \
as well as add it to the shade plugin `<includes>`, so that it's embedded in InjectableJar

If you add a dependency, that is already in the game classpath, then you don't have to shade it in \
If you do decide to add the dependency in the shaded jar, it won't override the one already in the game, \
so you might have some problems when the dependency version used in InjectableJar, is different from the version used in game (I got this issue with the asm library) \
To fix that issue, you can modify MemoryJarClassLoader, so that it doesn't delegate the class loading process to its parent (the game classloader), \
just like it's done for the asm dependency : [MemoryJarClassLoader.java](memory-jar-classloader/src/main/java/io/github/lefraudeur/internal/MemoryJarClassLoader.java) \
Once you made your changes, you will have to compile it, and write its bytes to [src/MemoryJarClassLoader.class.hpp](src/MemoryJarClassLoader.class.hpp)

# Mujina ?
This project is very similar to the previous one [Mujina-Public](https://github.com/Lefraudeur/Mujina-Public) \
Except this time, it focuses on the base, rather than the possible cheat modules you could do using it. \
There are also a lot of improvement on the jar loading method, it is now less likely to fail, and should be faster.\
It is also now way easier to make new events, and they don't use reflection anymore, so it should be more performant.

# Disclaimer
There is no guarrantee that this base can actually be used to build a functional cheat (it was not tested), or to fit any other particular purpose \
It is purely something to play with

Although the term "Injectable" is often associated with modifying minecraft in a more stealthy way, it is not the case at all for this base.\
This base isn't built to bypass any anticheats or detections of any sort.\
The project makes use of some jvmti features that might not be available on all jvms.

You are free to do anything you want using this project, except obviously :
- Redistributing while claiming ownership of the project
- Pretending to be me (happens a lot so be careful, I communicate only through discord: lefraudeur, which is linked to my github account)
