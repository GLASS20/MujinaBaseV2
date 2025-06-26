# MujinaBaseV2

An injectable java cheat base for minecraft with a cool event system, loaded with C++, that can remap to work on multiple clients such as vanilla, forge and lunar client

The base is for 1.7.10 minecraft, however it can be adapted to work on any minecraft version. \
It works only on windows, however it could be modified to work on linux fairly easily \
(replace building scripts, and add the linux version of ixwebsockets).

This project is very similar to my previous one [Mujina-Public](https://github.com/Lefraudeur/Mujina-Public)
Except this time, it focuses on the base, rather than the possible cheat modules you could do using it. \
Unlike Mujina-Public, MujinaBaseV2 is based on RuntimeJarLoader, which starts a local webserver to host the jar, and constructs a new URLClassLoader with the jar's url.

It's pretty easy to add a jar to your application class path, the jvm provides many interfaces to do that.\
However when working with minecraft, the game class/fields/methods names might be different from the ones you used to build the jar you wish to load, which requires you to remap the jar first.\
This base aims to simplify this process by providing preconfigured tools to do that.

# Building

# Event system
## Add a new event


# How can I access private fields/methods ?



# Working Principle
## Building process detailed
## What happens on inject


# How do I add a new version


# Disclaimer
There is no guarrantee that this base can actually be used to build a functional cheat, or to fit any other particular purpose \
It is purely something to play with

Although the term "Injectable" is often associated with modifying minecraft in a more stealthy way, it is not the case at all for this base.

This base isn't built to bypass any anticheats or detections of any sort.\
This base is only meant to work on "standard" minecraft clients with a "standard" jvm that implements all the common jni and jvmti features.\
Meaning vanilla minecraft client, or modded ones using a mod loader that applies modifications and common remapping at runtime (to yarn, srg, mcp, moj...).