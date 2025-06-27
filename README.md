# MujinaBaseV2

An injectable java cheat base for minecraft with a cool event system, loaded with C++, that can remap to work on multiple clients such as vanilla, forge and lunar client

The base is for 1.7.10 minecraft, however it can be adapted to work on any minecraft version. \
It works only on windows at the moment, however it could be modified to work on linux fairly easily \
(replace building scripts, and add the linux version of ixwebsockets).

It's pretty easy to add a jar to your application class path, the jvm provides many interfaces to do that.\
However when working with minecraft, the game class/fields/methods names might be different from the ones you used to build the jar you wish to load, which requires you to remap the jar first.\
This base aims to simplify this process by providing preconfigured tools to do that.

# Building

The project is meant to be fully portable, so it should work on any windows environment by running `build.bat`, without manually installing dependencies or building tools.\
However the maven project relies on multiple online repositories, which could break the build if they were to go offline.

# Using the base

The actual java cheat that's beeing injected is the maven project in the folder InjectableJar/InjectableJar.
It is the only part of the project you should need to modify most of the time as a user, and where you would code your modules.
> It can't be built alone, as it depends on the other projects InjectableJar-commons and InjectableJar-processor,\
> if you want to only build the java project, without remapping the resulting jar and embedding it in the dll, \
> run `mvn package` in the folder InjectableJar.

When coding, you have to bee extra careful about which threads your methods are beeing called from,\
ideally you would want everything to execute on the main thread,\
and if you have tasks you wish to perform based on the result coming from another thread, you can add them to an event queue that is flushed periodically from the game's main thread.



# Event system
## Add a new event
The @EventHandler annotation is first checked by the annotation processor for incoherent target method and event handler signature, and will fail the compilation in such cases.


# How can I access private fields/methods ?



# Working Principle
## Building process detailed
## What happens on inject


# How do I add a new version ?
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
- InjectableJar/pom.xml property : maven.compiler.release


# Mujina ?
This project is very similar to my previous one [Mujina-Public](https://github.com/Lefraudeur/Mujina-Public) \
Except this time, it focuses on the base, rather than the possible cheat modules you could do using it. \
Unlike Mujina-Public, MujinaBaseV2 is based on RuntimeJarLoader, which starts a local webserver to host the jar, and constructs a new URLClassLoader with the jar's url. \
Therefore, I've decided to reuse that name, credit to @lattiahirvio for coming up with the name.

It is very unlikely that the original Mujina project, which was supposed to be a fully functional client, will ever come to be, as I've decided to move away from minecraft, and so have other members.

# Disclaimer
There is no guarrantee that this base can actually be used to build a functional cheat (not tested), or to fit any other particular purpose \
It is purely something to play with

Although the term "Injectable" is often associated with modifying minecraft in a more stealthy way, it is not the case at all for this base.\
This base isn't built to bypass any anticheats or detections of any sort.\
As it uses objectweb asm library to edit .class files, it might struggle working on heavily obfuscated clients, that are meant to work on the jvm, but crash other .class analysis tools.\
The project makes use of some jvmti and jni features that might not be available on all jvms.