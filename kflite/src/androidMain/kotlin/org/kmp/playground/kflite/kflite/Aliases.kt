package org.kmp.playground.kflite.kflite

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

import com.google.ai.edge.litert.CompiledModel as LiteRTCompiledModel
import org.tensorflow.lite.Interpreter as TFLiteInterpreter
import org.tensorflow.lite.Tensor as TFLiteTensor

internal typealias PlatformTFLiteInterpreter = TFLiteInterpreter
internal typealias PlatformTFLiteInterpreterOptions = TFLiteInterpreter.Options
internal typealias PlatformTFLiteTensor = TFLiteTensor

internal typealias PlatformLiteRTCompiledModel = LiteRTCompiledModel
internal typealias PlatformLiteRTInterpreterOptions = LiteRTCompiledModel.Options



