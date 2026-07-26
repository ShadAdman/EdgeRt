package org.kmp.playground.kflite.kflite

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

import cocoapods.TFLTensorFlowLite.TFLTensor
import cocoapods.TFLTensorFlowLite.TFLInterpreter
import cocoapods.TFLTensorFlowLite.TFLInterpreterOptions

import cocoapods.executorch.ExecutorchModule
import cocoapods.executorch.ExecutorchEValue
import cocoapods.executorch.ExecutorchTensor

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal typealias PlatformInterpreter = TFLInterpreter
@OptIn(ExperimentalForeignApi::class)
internal typealias PlatformTensor = TFLTensor
@OptIn(ExperimentalForeignApi::class)
internal typealias PlatformInterpreterOptions = TFLInterpreterOptions

@OptIn(ExperimentalForeignApi::class)
internal typealias PlatformPytorchModule = ExecutorchModule
@OptIn(ExperimentalForeignApi::class)
internal typealias PlatformPytorchEValue = ExecutorchEValue
@OptIn(ExperimentalForeignApi::class)
internal typealias PlatformPytorchTensor = ExecutorchTensor






