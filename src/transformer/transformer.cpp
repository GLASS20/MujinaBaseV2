#include "transformer.hpp"
#include "../logger/logger.hpp"
#include <memory>
#include <fstream>

static void ClassFileLoadHook_callback(jvmtiEnv* jvmti_env, JNIEnv* jni_env,
	jclass class_being_redefined, jobject loader, const char* name,
	jobject protection_domain,
	jint class_data_len, const unsigned char* class_data,
	jint* new_class_data_len, unsigned char** new_class_data)
{
	jni::set_thread_env(jni_env);

	jni::frame frame{};

	maps::PatcherHelper PatcherHelperClass{};

	maps::ClassModifier classModifier = PatcherHelperClass.getClassModifier(maps::Class(class_being_redefined));
	if (!classModifier)
		return;

	jni::array<jbyte> class_bytes = jni::array<jbyte>::create(std::vector<jbyte>((jbyte*)class_data, (jbyte*)(class_data)+class_data_len));
	if (!class_bytes)
	{
		logger::error("failed to create original class byte array");
		return;
	}

	jni::array<jbyte> transformed_class_bytes = classModifier.patch(class_bytes);
	if (jni_env->ExceptionCheck())
	{
		jni_env->ExceptionDescribe();
		jni_env->ExceptionClear();
	}
	if (!transformed_class_bytes)
	{
		logger::error(std::string("failed to patch class bytes: ") + name);
		return;
	}

	std::vector<jbyte> transformed = transformed_class_bytes.to_vector();
	
	unsigned char* transformed_class_bytes_jvmti = nullptr;
	jvmti_env->Allocate(transformed.size(), &transformed_class_bytes_jvmti);
	memcpy(transformed_class_bytes_jvmti, transformed.data(), transformed.size());

	*new_class_data_len = transformed.size();
	*new_class_data = transformed_class_bytes_jvmti;
	return;
}

static bool retransform_classes(jvmtiEnv* env)
{
	jni::frame frame{};

	maps::PatcherHelper PatcherHelperClass{};

	std::vector<maps::Class> to_retransform = PatcherHelperClass.getClassesToTransform().to_vector();
	std::unique_ptr<jclass[]> to_retransform_jclasses = std::make_unique<jclass[]>(to_retransform.size());
	for (int i = 0; i < to_retransform.size(); ++i)
		to_retransform_jclasses[i] = jclass(to_retransform[i]);

	jvmtiError status = env->RetransformClasses(to_retransform.size(), to_retransform_jclasses.get());
	if (status != JVMTI_ERROR_NONE)
	{
		const char* error = "jvmti unknown error";
		env->GetErrorName(status, (char**)&error);
		logger::error(error);
		return false;
	}

	return true;
}

bool transformer::init(const jvmti& jvmti_instance, const maps::MemoryJarClassLoader& classLoader)
{
	// we need to manually give meta jni jclass instances, and they have to be global, because they will be used from a different thread
	jclass PatcherHelper_jclass = classLoader.loadClass(maps::String::create("io.github.lefraudeur.internal.patcher.PatcherHelper"));
	if (!PatcherHelper_jclass)
	{
		logger::error("failed to load PatcherHelperClass");
		return false;
	}
	PatcherHelper_jclass = (jclass)jni::get_env()->NewGlobalRef(PatcherHelper_jclass);
	jni::jclass_cache<maps::PatcherHelper>::value = PatcherHelper_jclass;

	jclass ClassModifier_jclass = classLoader.loadClass(maps::String::create("io.github.lefraudeur.internal.patcher.ClassModifier"));
	if (!ClassModifier_jclass)
	{
		logger::error("failed to load ClassModifier");
		return false;
	}
	ClassModifier_jclass = (jclass)jni::get_env()->NewGlobalRef(ClassModifier_jclass);
	jni::jclass_cache<maps::ClassModifier>::value = ClassModifier_jclass;

	maps::PatcherHelper PatcherHelperClass{};
	if (!PatcherHelperClass.init())
	{
		logger::error("failed to init PatcherHelper");
		return false;
	}


	jvmtiEnv* env = jvmti_instance.get_env();

	jvmtiCapabilities cap{};
	cap.can_retransform_classes = JVMTI_ENABLE;
	if (env->AddCapabilities(&cap) != JVMTI_ERROR_NONE)
	{
		logger::error("Retransform classes not supported");
		return false;
	}

	jvmtiEventCallbacks callbacks{};
	callbacks.ClassFileLoadHook = ClassFileLoadHook_callback;
	if (env->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks)) != JVMTI_ERROR_NONE)
	{
		logger::error("could not set event callback");
		return false;
	}

	if (env->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr) != JVMTI_ERROR_NONE)
	{
		logger::error("failed to enable event");
		return false;
	}

	if (!retransform_classes(env))
		return false;

	return true;
}

void transformer::shutdown(const jvmti& jvmti_instance)
{
	jvmtiEnv* env = jvmti_instance.get_env();

	env->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr);
	jvmtiEventCallbacks callbacks{};
	env->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks));

	retransform_classes(env);

	jni::get_env()->DeleteGlobalRef(jni::jclass_cache<maps::PatcherHelper>::value);
	jni::get_env()->DeleteGlobalRef(jni::jclass_cache<maps::ClassModifier>::value);

	jvmtiCapabilities cap{};
	cap.can_retransform_classes = JVMTI_ENABLE;
	env->RelinquishCapabilities(&cap);
	return;
}