# MujinaBaseV2

An injectable java cheat base for minecraft with a cool event system, loaded with C++, that can remap to work on multiple clients such as vanilla, forge and lunar client

The base is for 1.7.10 minecraft, however it can be adapted to work on any minecraft version. \
It works only on windows at the moment, however it could be modified to work on linux fairly easily \
(replace building scripts, and add jvm.so).

It's pretty easy to add a jar to your application class path, the jvm provides many interfaces to do that.\
However when working with minecraft, the game class/fields/methods names might be different from the ones you used to build the jar you wish to load, which requires you to remap the jar first.\
This base aims to simplify this process by providing preconfigured tools to do that.

It also allows you to setup events easily : [Event system](#event-system)
# Building

The project is meant to be fully portable, so it should work on any windows environment by running `build.bat`, without manually installing dependencies or building tools.\
However the maven project relies on multiple online repositories, which could break the build if they were to go offline.

Since you build using build.bat and not with your IDE, make sure you save all the files you edited with your IDE first.

# Using the base

The actual java cheat that's beeing injected is the maven project in the folder [InjectableJar/InjectableJar](InjectableJar/InjectableJar).\
It is the only part of the project you should need to modify most of the time as a user, and where you would code your modules.\
In this project, Minecraft renamed jar is included as a dependency, and you can refer Minecraft classes.
> It can't be built alone, as it depends on the other projects InjectableJar-commons and InjectableJar-processor, so make sure to open your IDE in the parent maven project\
> if you want to only build the java project, without remapping the resulting jar (source level remapping done by the annotation processor will still happen) and embedding it in the dll, \
> run `mvn package` in the folder InjectableJar.

I personally open intellij IDEA in the [InjectableJar](InjectableJar) folder, but you should be able to use any IDE.\
Just make sure to configure it to use the right jdk version (java 8)

When coding, you have to be extra careful about which threads your methods are beeing called from,\
ideally you would want everything to execute on the game's main thread,\
and if you have tasks you wish to perform based on the result coming from another thread, you can add them to an event queue that is flushed periodically from the game's main thread. ( a classic async architecture basically )


# Event system
The real hassle when making an injectable cheat in java, is not really adding the jar to the classpath, it usually takes only one line of code, \
but actually setting up "events", meaning adding instructions to a game's function so that it calls one of the method in your cheat jar. \
Otherwise you just have useless classes in the jvm that are never used. \
The @EventHandler annotation allows you to do that. \
Be aware that the @EventHandler annotation is first checked by the annotation processor for incoherent target method and event handler signature, and will fail the compilation in such cases. \
It is purposefully strict to avoid having surprises at runtime.
## Add a new event

# How do I call the original unmodified method ?
At the moment, there is no way to call the original method that won't trigger the event. \
So do not call the modified method from its event handler, otherwise you will end up in an infinite loop. \
The possible workarounds would be :
- Make a boolean that would tell your handler to immediatly return without doing anything (won't always work, not reliable)
- Reimplement the original method yourself (takes a lot of time, and will need to use reflection for private fields/methods)


# How can I access private fields/methods ?
The project does not include any easy way to access private fields/methods of the game, \
You will have to use java reflection api to do that \
However be aware that the strings you will use to make your reflection calls won't be remapped automatically. \
It is planned to include a way to remap these strings automatically in the future.


# Working Principle
## Building process detailed
## What happens on inject


# How do I adapt the project to work with another minecraft version ?
I've tried my best to explain what this project does in the previous sections, make sure to read them carefully, as well as check out the external references.

Some hints about what you might need to modify:
- InjectableJar/InjectableJar/remapper/1.7.10.tiny, to whatever mappings you wish to apply
- InjectableJar/InjectableJar/remapper/client.jar (original vanilla mineecraft client) from [minecraft version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json)
- InjectableJar/InjectableJar/remapper/client_named.jar / client_srg.jar, client.jar remapped using tiny remapper to understandable names for coding (named), and to other intermediary names (srg in this case)
- minecraft jar used as a dependency to code InjectableJar.jar (client_named.jar), defined in InjectableJar/pom.xml :
    ```xml
    <dependency>
        <groupId>minecraft.client</groupId>
        <artifactId>named</artifactId>
        <version>1.7.10</version>
    </dependency>
    ```
    So you will have to install the client_named.jar to local_maven_repo
- minecraft dependencies, listed in the corresponding [minecraft version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), and that are added to InjectableJar/pom.xml dependencies

And rarely, when you switch java version:
- jdk (jdk8u442-b06) and its reference in env.bat
- InjectableJar/pom.xml property : maven.compiler.release (java version has to be lower or equal to the one of the jvm you inject to)
- InjectableJar/InjectableJar/src/main/java/io/github/lefraudeur/internal/EventClassLoader.java
- asm library version and api version (Opcodes.ASMX) (shouldn't have to be updated, unless there is a new major java version)

# How do I add a dependency to my jar
Most of the time you should edit InjectableJar/InjectableJar/pom.xml `<dependencies>` \
as well as add it to the shade plugin `<includes>`, so that it's embedded in InjectableJar

If you add a dependency, that is already in the game classpath, then you don't have to shade it in \
If you do decide to add the dependency in the shaded jar, it won't override the one already in the game, \
so you might have some problems when the dependency version used in InjectableJar, is different from the version used in game (I got this issue with the asm library) \
To fix that issue, you can modify MemoryJarClassLoader, so that it doesn't delegate the class loading process to its parent (the game classloader),
just like it's done for the asm dependency : [MemoryJarClassLoader.java](memory-jar-classloader/src/main/java/io/github/lefraudeur/internal/MemoryJarClassLoader.java)

# Mujina ?
This project is very similar to my previous one [Mujina-Public](https://github.com/Lefraudeur/Mujina-Public) \
Except this time, it focuses on the base, rather than the possible cheat modules you could do using it. \
There are also a lot of improvement on the jar loading method, it is now less likely to fail, and should be faster.\
It is also now way easier to make new events, and they don't use reflection anymore, so it should be more performant.\
Therefore, I've decided to reuse that name, credit to  [lattiahirvio](https://github.com/lattiahirvio) for coming up with the name.

It is very unlikely that the original Mujina project, which was supposed to be a fully functional client, will ever come to be, as I've decided to move away from minecraft, and so have other members.

# Disclaimer
There is no guarrantee that this base can actually be used to build a functional cheat (not tested), or to fit any other particular purpose \
It is purely something to play with

When in the readme you encounter "it is planned to ... in the future", it only means that it's in my mind, and I know a way to do it so it's doable \
However it doesn't mean that I will implement it soon, could be in a month, a year, a decade or never.

Although the term "Injectable" is often associated with modifying minecraft in a more stealthy way, it is not the case at all for this base.\
This base isn't built to bypass any anticheats or detections of any sort.\
As it uses objectweb asm library to edit .class files, it might struggle working on heavily obfuscated clients, that are meant to work on the jvm, but crash other .class analysis tools.\
The project makes use of some jvmti and jni features that might not be available on all jvms.

You are free to do anything you want using this project, except obviously :
- Redistributing while claiming ownership of the project
- Pretending to be me (happens a lot so be careful, I communicate only through discord: lefraudeur, which is linked to my github account)
