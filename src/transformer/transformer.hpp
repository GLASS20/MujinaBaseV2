#pragma once
#include "../jvmti/jvmti.hpp"

namespace transformer
{
	bool init(const jvmti& jvmti_instance, const maps::MemoryJarClassLoader& classLoader);
	void shutdown(const jvmti& jvmti_instance);
}