package org.kmp.playground.kflite

import com.google.ai.edge.litert.CompiledModel as LiteRTCompiledModel
import org.tensorflow.lite.Interpreter as TFLiteInterpreter
import org.tensorflow.lite.Tensor as TFLiteTensor

internal typealias PlatformTFLiteInterpreter = TFLiteInterpreter
internal typealias PlatformTFLiteInterpreterOptions = TFLiteInterpreter.Options
internal typealias PlatformTFLiteTensor = TFLiteTensor

internal typealias PlatformLiteRTCompiledModel = LiteRTCompiledModel
internal typealias PlatformLiteRTInterpreterOptions = LiteRTCompiledModel.Options

