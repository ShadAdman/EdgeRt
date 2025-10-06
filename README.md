<a href="http://www.wtfpl.net/"><img
src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
width="80" height="15" alt="WTFPL" /></a>
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-green.svg?style=flat-square&logo=gradle)](https://gradle.org/)

![](poster.jpg)
<p align="center"> Kflite is a fresh, improved version of <a href="https://github.com/icerockdev/moko-tensorflow">moko-tensorflow</a> with better support. </br>It uses `composeResources` and requires no platform-specific code.</p>

## Overview
Kflite lets you load and run TensorFlow Lite (TFLite) models directly from your shared Kotlin code.
It abstracts platform differences, and manages model loading, tensor creation, and inference through a unified API.

Key advantages:

- Works seamlessly with Compose Multiplatform using `composeResources`
- Requires **no platform-specific setup** for Android or Desktop
- Supports model normalization (`YOLO`, `COCO`, `PascalVOC`, `TF formats`)
- Lightweight — minimal dependencies and zero reflection overhead

## Getting Started
For the fastest setup and a working example, see **[KfliteSample](https://github.com/shadmanadman/kflite-sample)**. </br>
It demonstrates a full pipeline.

## Installation
### Step 1 - Add dependencies
Add this to your `commonMain.dependencies` :

``` gradle
implementation("io.github.shadmanadman:kflite:0.70.1")
```
### Step 2 - Configure CocoaPods for iOS
Since KMP doesn’t automatically include CocoaPods dependencies, you need to manually add TensorFlow Lite for iOS. </br>
Configure your project for CocoaPods and include:
``` gradle
iosX64()
iosArm64()
iosSimulatorArm64()


cocoapods {
    summary = "Some description for the Shared Module"
    homepage = "Link to the Shared Module homepage"
    version = "1.0"
    ios.deploymentTarget = "16.0"
    podfile = project.file("../iosApp/Podfile")
    pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
    framework {
        baseName = "ComposeApp"
        isStatic = true
        linkerOpts(
            project.file("../iosApp/Pods/TensorFlowLiteC/Frameworks").path.let { "-F$it" },
            "-framework", "TensorFlowLiteC"
        )
    }
}
```
If you get the following error during ios build:
``` bash
clang: error: linker command failed with exit code 1 (use -v to see invocation)
```
That is a linker error. It simply means the Cocoapods framework is not linked correctly to your ios app.


## Using a Model

### Step 1 - Place model
`kflite` uses the new compose resources. So you just place your `.tflite` model in the `composeResources->files` folder. 

This makes it available to both Android and iOS via `Res.readBytes()`.


### Step 2 - Initialize the model
No platform-specific code needed — everything runs in `commonMain`.

Kflite loads and prepares your model for inference with optional performance parameters. </br>
Call init on `Kflite` and pass the model as byte array.
 - options is not mandatory. Set values carefully, Make sure your model supports each one.
``` kotlin
Kflite.init(
    model = Res.readBytes("files/efficientdet-lite2.tflite"),
    options = InterpreterOptions(
        numThreads = 4,
        delegateType = DelegateType.NNAPI_COREML, // Uses NNAPI on Android, CoreML on iOS
        allowFp16PrecisionForFp32 = true
    )
)
```
- `numThreads`: controls CPU parallelism
- `delegateType`: selects hardware acceleration backend
- `allowFp16PrecisionForFp32`: speeds up inference if supported by hardware

### Step 3 - Prepare the input data
Kflite works with direct `ByteBuffer` input, so you can feed preprocessed images or tensors directly.
``` kotlin
// Prepare input data: Example model takes 4D array as an input, an image with 480x480 pixels
val inputImageWidth = Kflite.getInputTensor(0).shape[1]
val inputImageHeight = Kflite.getInputTensor(0).shape[2]
val modelInputSize =
    FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE
      
// Creates ByteBuffer to hold the image data    
val inputImage =  imageResource(Res.drawable.example_model_input)
    .toScaledByteBuffer(
        inputWidth = inputImageWidth,
        inputHeight = inputImageHeight,
        inputAllocateSize = modelInputSize
    )   
```
This example scales an image to match model input size and converts it into a normalized float array.

### Step 4 - Prepare the output data
``` kotlin
// Prepare output data: Example model has 3D array as an output
val firstOutputShape = Kflite.getOutputTensor(0).shape[0]
val secondOutputShape = Kflite.getOutputTensor(0).shape[1]
val thirdOutputShape = Kflite.getOutputTensor(0).shape[2]

val modelOutputContainer = Array(firstOutputShape) {
    Array(secondOutputShape) {
        FloatArray(thirdOutputShape)
    }
}
```
This container will hold the model’s inference output (e.g., object coordinates, class scores, etc.).

### Step 5 - Run and close the model:
``` kotlin
Kflite.run(listOf(inputImage), mapOf(Pair(0,modelOutputContainer)))
// Close the model after use
Kflite.close()
```
Once closed, all underlying TFLite interpreters are released.

## Normalizing Model Output
Most detection models output bounding boxes in model-scaled coordinates. </br>
Use Kflite’s Normalization utility to convert them back to the original image scale.
``` kotlin
val normalizedBox = Normalization(
    originalImageHeight = 1080f, //Original input height
    originalImageWidth = 2010f, // Original input width
    modelImagWidth = 680f, //Model input width
    modelImageHeight = 680f //Model input height
).YOLO(
    center_x = 20f, //CenterX of Model Output From The Model
    center_y = 20f,//CenterY of Model Output From The Model
    width = 100f,  //Width of Model Output From The Model
    height = 120f //Height of Model Output From The Model
)
```
The `normalizedBox` will be a data class contain the new ordinations. You can use it to point the object 
or create a bounding box.

**Other supported Formats:**
- `Normalization.pascalVOC(x_min, y_min, x_max, y_max)`
- `Normalization.coco(x, y, width, height)`
- `Normalization.yolo(cx, cy, width, height)`
- `Normalization.tfObjectDetection(top, left, bottom, right)`
- `Normalization.tfRecordVariant(x_min, y_min, x_max, y_max)`

## What's next
- Live detection with Camera feed

## Licence
```
               DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2025 Shadman Adman <adman.shadman@gmail.com> 

 Everyone is permitted to copy and distribute kflite or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

0. You just DO WHAT THE FUCK YOU WANT TO.
```


