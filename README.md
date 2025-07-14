# MujinaBaseV2

An injectable java cheat base for minecraft with a cool event system, loaded with C++, that can remap to work on vanilla and forge

The base is for 1.7.10 minecraft, however it should be adaptable to any minecraft version. \
It works only on windows at the moment, however it could be modified to work on linux fairly easily \
(replace building scripts, and add jvm.so).

THIS IS NOT A WAY TO INJECT FORGE / FABRIC MODS, it only allows you to build an injectable jar that can access minecraft classes, as well as slightly modify existing minecraft methods' implementations using events.\
Which means to actually make use of that base, you need to understand the minecraft source code, and know exactly which part of it you wish to modify, and what you wanna add to it.
# Building

The project is meant to be fully portable, so it should work on any windows environment by running [build_srg.bat](build_srg.bat) (for forge) or [build_vanilla.bat](build_vanilla.bat) (for vanilla), without manually installing dependencies or building tools.\
However the maven project relies on multiple online repositories, which could break the build if they were to go offline.

Since you don't build using your IDE, make sure you save all the files you edited with your IDE first.

If security is a concern, you can use the jdk / mave / cmake / visual studio build tools you downloaded yourself from a trusted source, just make sure that they are in your PATH, or edit [env.bat](env.bat)

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

There are some examples in [TestClass.java](InjectableJar/InjectableJar/src/main/java/io/github/lefraudeur/TestClass.java)

### Add a new event handler
#### On entry event handler
This can be used to add a call to your event handler at the beginning of a method. \
To declare an event handler you have to define a public static method and annotate with @EventHandler :
```JAVA
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
```JAVA
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

# Known issues
- The classes aren't unloaded properly
- On reinject, some modifications made to the jar might not be taken into account, because it will use the previous classes that haven't been unloaded properly. (This is likely due to the retransformed classes keeping resolved event handler method in cache, which prevent them from beeing unloaded, possible fix : use reflection, so that resolved methods aren't cached, problem : bad performance because it will have to lookup for the methods using symbolic names every time)
- It leaves a lot of traces (not fixable)
- The classloader trick I use for the modified method to be able to call event handlers might not work on all clients, for example on lunar, the classLoader does not necessarily forward the loadClass to its parent first, except for some internal jdk classes, which breaks the trick. (using reflection will also fix that, at the cost of performance, since the classloader trick won't be needed anymore)

# Working Principle
## Building process detailed
### 1. InjectableJar parent maven project is built :

- Child [InjectableJar/InjectableJar-commons](InjectableJar/InjectableJar-commons) is built

- Child [InjectableJar/InjectableJar-processor](InjectableJar/InjectableJar-processor) is built

- Child [InjectableJar/InjectableJar](InjectableJar/InjectableJar) is built, which causes the [annotation processor](InjectableJar/InjectableJar-processor) to be run :

    For more informations about annotation processors see : [Processor javadoc](https://docs.oracle.com/en/java/javase/22/docs/api/java.compiler/javax/annotation/processing/Processor.html)
    and [javac annotation processing](https://docs.oracle.com/en/java/javase/22/docs/specs/man/javac.html#annotation-processing) \
    The annotation processor will read the @EventHandler annotations and generate the class : io.github.lefraudeur.internal.patcher.Patcher, you can see the generated source file in `target/generated-sources`\
    This class will regroup all required informations about the target classes and the transformations to apply, the annotation processor will also remap the names given to the @EventHandler annotations using [FabricMC mapping-io](https://github.com/FabricMC/mapping-io). \
    It will use the properties `remapper.mappingFilePath`, `remapper.sourceNamespace`, `remapper.destinationNamespace` defined in [InjectableJar/pom.xml](InjectableJar/pom.xml), which can be overridden from the CLI,the `remapper.destinationNamespace` property is overridden in [build_vanilla.bat](build_vanilla.bat) : `mvn package -Dremapper.destinationNamespace=obfuscated`. \
    Example generated class source file (unreadable lol):
    ```JAVA
    package io.github.lefraudeur.internal.patcher;
    import io.github.lefraudeur.internal.patcher.MethodModifier.MethodModifierInfo;

    public class Patcher
    {
        public final static ClassModifier[] classModifiers = 
        {
            new ClassModifier("bjk", new MethodModifier[]{new EntryMethodModifier(new MethodModifierInfo("a", "(Ljava/lang/String;)V", "io/github/lefraudeur/TestClass", "sendChatMessage", "(Lio/github/lefraudeur/internal/Canceler;Lbjk;Ljava/lang/String;)V", false)), new ReturnThrowMethodModifier(new MethodModifierInfo("a", "(Ljava/lang/String;)V", "io/github/lefraudeur/TestClass", "sendChatMessage", "(Lio/github/lefraudeur/internal/Thrower;Lbjk;Ljava/lang/String;)V", false))}),
            new ClassModifier("net/minecraft/client/ClientBrandRetriever", new MethodModifier[]{new ReturnThrowMethodModifier(new MethodModifierInfo("getClientModName", "()Ljava/lang/String;", "io/github/lefraudeur/TestClass", "getClientModName", "(Ljava/lang/String;Lio/github/lefraudeur/internal/Thrower;)Ljava/lang/String;", true))}),
            new ClassModifier("aji", new MethodModifier[]{new EntryMethodModifier(new MethodModifierInfo("a", "(Lahl;IIII)Z", "io/github/lefraudeur/TestClass", "shouldSideBeRendered", "(Lio/github/lefraudeur/internal/Canceler;Laji;Lahl;IIII)Z", false))})
        };
    }
    ```
    This is an array of ClassModifier, each ClassModifier containing the target class's name that has to be modified. Giving the original class bytes, a ClassModifier is able to generate the transformed class bytes by applying a chain of transformations, each transformation beeing represented by a MethodModifier. The class bytes are edited using the asm library. The [asm user guide](https://asm.ow2.io/asm4-guide.pdf) is a great resource to learn how to use the library, and to learn java bytecode in general. Take also a look at the [Java Virtual Machine Specification](https://docs.oracle.com/javase/specs/jvms/se24/html/index.html).
    The generated Patcher class is used by the [PatcherHelper](InjectableJar/InjectableJar/src/main/java/io/github/lefraudeur/internal/patcher/PatcherHelper.java) class, whose methods are called by [transformer.cpp](src/transformer/transformer.cpp). The role of [transformer.cpp](src/transformer/transformer.cpp) is to call jvmti [RetransformClasses](https://docs.oracle.com/en/java/javase/22/docs/specs/jvmti.html#RetransformClasses) on the classes that has to be modified (listed by the Patcher class generated by the annotation processor). This jvmti api allows to replace the definition of a loaded class at runtime.

    Before building the project, it will also copy the non shaded dependencies to `InjectableJar\InjectableJar\remapper\libs`, which will be given to tiny remapper as a class path.

### 2. InjectableJar-1.0-SNAPSHOT-shaded.jar is remapped
Your classes now have symbolic references to minecraft classes / methods / fields using human readable names (mcp mappings in this case), however the minecraft client in which we wish to inject, probably doesn't use these names, so it has to be remapped, so that the references can be successfully resolved at runtime.

This is done using [FabricMC tiny remapper](https://github.com/FabricMC/tiny-remapper), the tool is used as follows :
```SH
java -jar tiny-remapper-0.9.0-fat.jar input.jar output_remapped.jar mappings_file.tiny source_namespace destination_namespace additional_class_paths...
```
`source_namespace`and `destinatino_namespace` must be namespaces' names defined by `mappings_file.tiny` \
`additional_class_paths` are paths to any jars your input.jar might depend on. 

### 3. Remapped InjectableJar-1.0-SNAPSHOT-shaded.jar's bytes are written in `InjectableJar.jar.hpp`
This is done using ignore_File2Hex.exe, which will convert any file in the working directory that doesn't start with ignore_ and doesn't end with .hpp, into the corresponding .hpp file. \
The source code for that program can be found here : [File2hex/main.cpp](File2Hex/main.cpp)

### 4. The C++ cmake project is built
```SH
cmake -B build -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=RelWithDebInfo -DMINECRAFT_CLASS="bao"
cmake --build build
```
The dll needs to be aware of the Minecraft class name, so that it can get its class loader. For [build_vanilla.bat](build_vanilla.bat), it's "bao", as written in [1.7.10.tiny]()

## What happens on inject
Writing in progress, read the src for now

The c++ part makes use of [MetaJNI](https://github.com/Lefraudeur/MetaJNI) to make jni usage clearer.

# FAQ

## How do I call the original unmodified method ?
At the moment, there is no way to call the original method that won't trigger the event. \
So do not call the modified method from its event handler, otherwise you will end up in an infinite loop. \
The possible workarounds would be :
- Make a boolean that would tell your handler to immediatly return without doing anything (not reliable)
- Reimplement the original method yourself (takes a lot of time, and will need to use reflection for private fields/methods)


## How can I access private fields/methods ?
The project does not include any easy way to access private fields/methods of the game, \
You will have to use java reflection api to do that \
However be aware that the strings you will use to make your reflection calls won't be remapped automatically.

## How do I adapt the project to work with another minecraft version ?
I've tried my best to explain the structure of this project in the previous sections, make sure to read them carefully, as well as check out the external references.

Some hints about what you might need to modify:
- [InjectableJar/InjectableJar/remapper/1.7.10.tiny](InjectableJar/InjectableJar/remapper/1.7.10.tiny), to whatever mappings you wish to apply,\
the way to get these mappings file depends on your needs and on your target minecraft version, they come in different format, but you can combine them and convert them easily into a single .tiny file using [mapping-io](https://github.com/FabricMC/mapping-io)
- [InjectableJar/InjectableJar/remapper/client.jar](InjectableJar/InjectableJar/remapper/client.jar) (original vanilla minecraft client) from [minecraft version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json)
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
    ```SH
    mvn org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file -Dfile=../client_named.jar -DgroupId=your.groupId -DartifactId=your-artifactId -Dversion=version -Dpackaging=jar -DlocalRepositoryPath=local_maven_repo
    ```
- minecraft dependencies, listed in the corresponding [minecraft version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), and that are added to [InjectableJar/pom.xml](InjectableJar/pom.xml) dependencies
- [InjectableJar/pom.xml](InjectableJar/pom.xml) properties : remapper.mappingFilePath, remapper.sourceNamespace, remapper.destinationNamespace which can be overridden in the `mvn package` command using `-Dremapper.sourceNamespace=value`
- build script 

And rarely, when you switch java version:
- jdk (jdk8u442-b06) and its reference in env.bat
- InjectableJar/pom.xml property : maven.compiler.release (java version has to be lower or equal to the one of the jvm you inject to)
- asm library version and api version (Opcodes.ASMX)

## How do I add a dependency to my jar ?
Most of the time you should edit [InjectableJar/InjectableJar/pom.xml](InjectableJar/InjectableJar/pom.xml) `<dependencies>` \
as well as add it to the shade plugin `<includes>`, so that it's embedded in InjectableJar

If you add a dependency, that is already in the game classpath, then you don't have to shade it in \
If you do decide to add the dependency in the shaded jar, it won't override the one already in the game, \
so you might have some problems when the dependency version used in InjectableJar, is different from the version used in game (I got this issue with the asm library) \
To fix that issue, you can modify MemoryJarClassLoader, so that it doesn't delegate the class loading process to its parent (the game classloader), \
just like it's done for the asm dependency : [MemoryJarClassLoader.java](memory-jar-classloader/src/main/java/io/github/lefraudeur/internal/MemoryJarClassLoader.java) \
Once you made your changes, you will have to compile it, and write its bytes to [src/MemoryJarClassLoader.class.hpp](src/MemoryJarClassLoader.class.hpp)

## How do I inject ?
Idk

## Mujina ?
This project is very similar to the previous one [Mujina-Public](https://github.com/Lefraudeur/Mujina-Public) \
Except this time, it focuses on the base, rather than the possible cheat modules you could do using it. \
There are also a lot of improvement on the jar loading method, it is now less likely to fail, and should be faster.\
It is also now way easier to make new events, and they don't use reflection anymore, so it should be more performant.

# Disclaimer
There is no guarrantee that this base can actually be used to build a functional cheat (it was not tested), or to fit any other particular purpose \
It is purely something to play with. \
I encourage you to use this base as a resource to build your own, that will fit your needs. I use a lot of FabricMC tools, because they are easy to use, but you can also use the ones proposed by Forge, or you can make your own tools.

You can contact me on discord, but If I don't answer you then don't insist. There is a good chance I've seen your message, but I didn't want to answer you. (I have no obligation to do so)

Don't expect me to update / maintain this project.
Don't cheat

Although the term "Injectable" is often associated with modifying minecraft in a more stealthy way, it is not the case at all for this base.\
This base isn't built to bypass any anticheats or detections of any sort.\
The project makes use of some jvmti features that might not be available on all jvms.

You are free to do anything you want using this project, except obviously :
- Redistributing while claiming ownership of the project
- Pretending to be me (happens a lot so be careful, I communicate only through discord: lefraudeur, which is linked to my github account)