package org.kmp.playground.edgert.edgert

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

import cocoapods.TFLTensorFlowLite.TFLTensor
import cocoapods.TFLTensorFlowLite.TFLInterpreter
import cocoapods.TFLTensorFlowLite.TFLInterpreterOptions

import swiftPMImport.io.github.shadadman.edgert.ExecutorchModule
import swiftPMImport.io.github.shadadman.edgert.ExecutorchEValue
import swiftPMImport.io.github.shadadman.edgert.ExecutorchTensor

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








