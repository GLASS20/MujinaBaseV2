#ifdef _WIN32
    #include <Windows.h>
#elif defined(__linux__)
    #include <X11/Xlib.h>
    #include <X11/Xutil.h>
#endif

#include "meta_jni.hpp"
#include "mappings.hpp"
#include "InjectableJar.jar.hpp"
#include "MemoryJarClassLoader.class.hpp"
#include "jvmti/jvmti.hpp"
#include "logger/logger.hpp"
#include "transformer/transformer.hpp"
#include <thread>
#include <iostream>

#ifndef MINECRAFT_CLASS
# define MINECRAFT_CLASS "net/minecraft/client/Minecraft"
#endif


#ifdef __linux__
static Display* display = nullptr;
#endif

static bool is_uninject_key_pressed()
{
#ifdef _WIN32
    return GetAsyncKeyState(VK_END);
#elif __linux__
    static KeyCode keycode = XKeysymToKeycode(display, XK_End);

    char key_states[32] = { '\0' };
    XQueryKeymap(display, key_states);

    // <<3 same as /8 (logic 2^3 = 8) and &7 same as %8 (idk y)
    return (key_states[keycode << 3] & (1 << (keycode & 7)));
#endif
}

static void mainFrame(const jvmti& jvmti_instance)
{
    jni::frame frame{}; // every local ref follow this frame object lifetime


    maps::Class minecraft_class = jvmti_instance.find_loaded_class(MINECRAFT_CLASS);
    if (!minecraft_class)
        return logger::error("failed to get minecraft_class");

    maps::ClassLoader minecraft_classloader = jvmti_instance.get_class_ClassLoader(minecraft_class);
    if (!minecraft_classloader)
        return logger::error("failed to get minecraft_classloader");



    // create a new classloader, and make it define the MemoryJarClassLoader class
    maps::SecureClassLoader secureClassLoader = maps::SecureClassLoader::new_object(&maps::SecureClassLoader::constructor);
    if (!secureClassLoader)
        return logger::error("failed to create secureClassLoader");

    jclass MemoryJarClassLoaderClass = jni::get_env()->DefineClass("io/github/lefraudeur/internal/MemoryJarClassLoader", secureClassLoader, (jbyte*)MemoryJarClassLoader_class.data(), MemoryJarClassLoader_class.size());
    if (!MemoryJarClassLoaderClass)
        return logger::error("failed to define MemoryJarClassLoader class");

    // tell MetaJni the MemoryJarClassLoader jclass it needs to use
    jni::jclass_cache<maps::MemoryJarClassLoader>::value = MemoryJarClassLoaderClass;




    jni::array<jbyte> InjectableJarJbyteArray = jni::array<jbyte>::create(std::vector<jbyte>(InjectableJar_jar.begin(), InjectableJar_jar.end()));
    maps::MemoryJarClassLoader classLoader = maps::MemoryJarClassLoader::new_object(&maps::MemoryJarClassLoader::constructor, InjectableJarJbyteArray, (maps::ClassLoader)minecraft_classloader);
    if (!classLoader)
        return logger::error("failed to create memoryJarClassLoader");

    // TODO: Make meta jni custom findClass that uses the classLoader we just created

    // metaJNI uses env->findClass to get the jclass, however our Jar isn't in SystemClassLoader search path
    jni::jclass_cache<maps::Main>::value = classLoader.loadClass(maps::String::create("io.github.lefraudeur.Main"));
    if (!jni::jclass_cache<maps::Main>::value)
        return logger::error("failed to find io.github.lefraudeur.Main");
    logger::log("loaded main class");

    // Setup the classLoader trick so that minecraft classes can access the cheat classes
    jni::jclass_cache<maps::EventClassLoader>::value = classLoader.loadClass(maps::String::create("io.github.lefraudeur.internal.EventClassLoader"));
    if (!jni::jclass_cache<maps::EventClassLoader>::value)
        return logger::error("failed to find io.github.lefraudeur.internal.EventClassLoader");
    maps::EventClassLoader eventClassLoader = maps::EventClassLoader::new_object(&maps::EventClassLoader::constructor, minecraft_classloader.parent.get(), (maps::ClassLoader)classLoader);

    minecraft_classloader.parent = (maps::ClassLoader)eventClassLoader;

    if (!transformer::init(jvmti_instance, classLoader))
    {
        minecraft_classloader.parent = eventClassLoader.parent.get();
        return;
    }


    // we now call the onLoad method which should send hello in chat
    // Console output might be broken if you used AllocConsole(), consider using another method to check whether the jar is loaded
    maps::Main Main{};
    Main.onLoad();

    while (!is_uninject_key_pressed())
    {
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
    }

    Main.onUnload();

    transformer::shutdown(jvmti_instance);

    minecraft_classloader.parent = eventClassLoader.parent.get();
}

static void app()
{

#ifdef _WIN32
    
#elif defined(__linux__)
    display = XOpenDisplay(NULL);
#endif

    JavaVM* jvm = nullptr;
    JNI_GetCreatedJavaVMs(&jvm, 1, nullptr);
    JNIEnv* env = nullptr;
    jvm->AttachCurrentThread((void**)&env, nullptr);
    jni::init();
    jni::set_thread_env(env); //this is needed for every new thread that uses the lib

    jvmti jvmti_instance{ jvm };
    if (jvmti_instance)
    {
        mainFrame(jvmti_instance);
        // use after jni::shutdown but normal
        jni::frame f{};
        jvmti_instance.get_env()->ForceGarbageCollection();
        jclass test = jvmti_instance.find_loaded_class("io/github/lefraudeur/Main");
        if (!test)
            logger::log("successfully unloaded classes");
        else
            logger::log("failed to unload classes");
    }


    jni::shutdown();
    jvm->DetachCurrentThread();

#ifdef _WIN32

#elif defined(__linux__)
    XCloseDisplay(display);
#endif
}

static void mainThread(void* dll)
{
    if (!logger::init())
        return;
    app();
    logger::log("unloaded");
    logger::shutdown();

#ifdef _WIN32
    FreeLibraryAndExitThread((HMODULE)dll, 0);
#endif
    return;
}

#ifdef _WIN32

BOOL WINAPI DllMain(
    HINSTANCE hinstDLL,  // handle to DLL module
    DWORD fdwReason,     // reason for calling function
    LPVOID lpvReserved)  // reserved
{
    // Perform actions based on the reason for calling.
    switch (fdwReason)
    {
    case DLL_PROCESS_ATTACH:
        // Initialize once for each new process.
        // Return FALSE to fail DLL load.
        CloseHandle(CreateThread(nullptr, 0, (LPTHREAD_START_ROUTINE)mainThread, hinstDLL, 0, 0));
        break;

    case DLL_THREAD_ATTACH:
        // Do thread-specific initialization.
        break;

    case DLL_THREAD_DETACH:
        // Do thread-specific cleanup.
        break;

    case DLL_PROCESS_DETACH:

        if (lpvReserved != nullptr)
        {
            break; // do not do cleanup if process termination scenario
        }

        // Perform any necessary cleanup.
        break;
    }
    return TRUE;  // Successful DLL_PROCESS_ATTACH.
}

#elif defined(__linux__)

void __attribute__((constructor)) onload_linux()
{
    pthread_t thread = 0U;
    pthread_create(&thread, nullptr, (void* (*)(void*))mainThread, nullptr);
    return;
}
void __attribute__((destructor)) onunload_linux()
{
    return;
}

#endif