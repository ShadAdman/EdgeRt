package org.kmp.playground.kflite.core.runtime

import org.kmp.playground.kflite.core.model.ModelSource
import org.kmp.playground.kflite.core.interpreter.InterpreterOptions

object RuntimeFactory {
    private val creators = mutableMapOf<RuntimeType, (ModelSource, InterpreterOptions) -> Runtime>()

    fun register(type: RuntimeType, creator: (ModelSource, InterpreterOptions) -> Runtime) {
        creators[type] = creator
    }

    fun create(modelSource: ModelSource, options: InterpreterOptions): Runtime {
        return creators[options.runtime]?.invoke(modelSource, options)
            ?: error("Runtime ${options.runtime} not registered. Make sure to depend on the appropriate runtime module.")
    }
}
