#ifdef __OBJC__
#import <UIKit/UIKit.h>
#else
#ifndef FOUNDATION_EXPORT
#if defined(__cplusplus)
#define FOUNDATION_EXPORT extern "C"
#else
#define FOUNDATION_EXPORT extern
#endif
#endif
#endif

#import "TFLDelegate.h"
#import "TFLInterpreter.h"
#import "TFLInterpreterOptions.h"
#import "TFLQuantizationParameters.h"
#import "TFLSignatureRunner.h"
#import "TFLTensor.h"
#import "TFLTensorFlowLite.h"
#import "TFLCoreMLDelegate.h"
#import "TFLMetalDelegate.h"

FOUNDATION_EXPORT double TFLTensorFlowLiteVersionNumber;
FOUNDATION_EXPORT const unsigned char TFLTensorFlowLiteVersionString[];

