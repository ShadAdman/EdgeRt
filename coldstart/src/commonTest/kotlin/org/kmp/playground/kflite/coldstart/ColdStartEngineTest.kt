package org.kmp.playground.kflite.coldstart

import org.kmp.playground.kflite.interpreter.InterpreterOptions
import org.kmp.playground.kflite.model.ByteArraySource
import org.kmp.playground.kflite.model.ModelSource
import org.kmp.playground.kflite.tensor.TensorDataType
import kotlin.test.*

class ColdStartEngineTest {

    private class FakeKfliteProxy : KfliteProxy {
        var initCalled = 0
        var runCount = 0
        var closeCalled = 0
        override var isInitialized = false

        override fun init(modelSource: ModelSource, options: InterpreterOptions) {
            initCalled++
            isInitialized = true
        }

        override fun getInputTensorCount(): Int = 1
        override fun getOutputTensorCount(): Int = 1
        override fun getInputTensorInfo(index: Int): TensorInfo = 
            TensorInfo("input", intArrayOf(1, 224, 224, 3), TensorDataType.FLOAT32)
        override fun getOutputTensorInfo(index: Int): TensorInfo = 
            TensorInfo("output", intArrayOf(1, 1000), TensorDataType.FLOAT32)

        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            runCount++
        }

        override fun close() {
            closeCalled++
            isInitialized = false
        }
    }

    @Test
    fun testDefaultConfiguration() {
        val fake = FakeKfliteProxy()
        val engine = ColdStartEngine(ByteArraySource(byteArrayOf()), InterpreterOptions(), ColdStartConfig(), { fake })
        engine.run()

        assertEquals(1, fake.initCalled)
        assertEquals(1, fake.runCount)
        assertEquals(1, fake.closeCalled)
    }

    @Test
    fun testCustomIterationCount() {
        val fake = FakeKfliteProxy()
        val engine = ColdStartEngine(ByteArraySource(byteArrayOf()), InterpreterOptions(), ColdStartConfig(iterations = 5), { fake })
        engine.run()

        assertEquals(5, fake.runCount)
    }

    @Test
    fun testCustomInputProvider() {
        var inputCreated = 0
        val customProvider = object : InputProvider {
            override fun createInput(tensor: TensorInfo): Any {
                inputCreated++
                return FloatArray(1)
            }
            override fun createOutput(tensor: TensorInfo): Any = FloatArray(1)
        }

        val fake = FakeKfliteProxy()
        val engine = ColdStartEngine(ByteArraySource(byteArrayOf()), InterpreterOptions(), ColdStartConfig(inputProvider = customProvider), { fake })
        engine.run()

        assertEquals(1, inputCreated)
    }

    @Test
    fun testCloseInterpreterFalse() {
        val fake = FakeKfliteProxy()
        val engine = ColdStartEngine(ByteArraySource(byteArrayOf()), InterpreterOptions(), ColdStartConfig(closeInterpreter = false), { fake })
        engine.run()

        assertEquals(0, fake.closeCalled)
        assertTrue(fake.isInitialized)
    }
}
