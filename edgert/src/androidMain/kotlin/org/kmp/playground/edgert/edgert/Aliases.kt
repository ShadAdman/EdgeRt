package org.kmp.playground.edgert.edgert

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

import com.google.ai.edge.litert.CompiledModel as LiteRTCompiledModel
import org.tensorflow.lite.Interpreter as TFLiteInterpreter
import org.tensorflow.lite.Tensor as TFLiteTensor

internal typealias PlatformTFLiteInterpreter = TFLiteInterpreter
internal typealias PlatformTFLiteInterpreterOptions = TFLiteInterpreter.Options
internal typealias PlatformTFLiteTensor = TFLiteTensor

internal typealias PlatformLiteRTCompiledModel = LiteRTCompiledModel
internal typealias PlatformLiteRTInterpreterOptions = LiteRTCompiledModel.Options





