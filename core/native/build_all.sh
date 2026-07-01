#!/bin/bash
set -e

# This script builds LiteRT for various platforms using CMake

LITERT_SRC="LiteRT"
GENERATED_DIR="../../generated"

build_platform() {
    PLATFORM=$1
    ARCH=$2
    OS_NAME=$3

    echo "Building for $PLATFORM ($ARCH)..."

    BUILD_DIR="build_$PLATFORM"
    mkdir -p $BUILD_DIR
    cd $BUILD_DIR

    # Configure CMake (simplified, need platform-specific toolchains for iOS etc.)
    cmake ../$LITERT_SRC/litert \
        -DCMAKE_BUILD_TYPE=Release \
        -DLITERT_BUILD_DYNAMIC_LIB=ON

    cmake --build . --config Release -j 8

    # Copy artifacts
    DEST="$GENERATED_DIR/$OS_NAME"
    mkdir -p "$DEST/lib"
    mkdir -p "$DEST/include"

    # Assuming the output library name is liblitert.so/dylib/dll
    cp *.so "$DEST/lib/" 2>/dev/null || cp *.dylib "$DEST/lib/" 2>/dev/null || cp *.dll "$DEST/lib/" 2>/dev/null

    # Copy headers (this is more complex in reality)
    cp -r ../$LITERT_SRC/litert/c/*.h "$DEST/include/"

    cd ..
}

# Example usages:
# build_platform "linux" "x64" "linux"
# build_platform "macos" "arm64" "mac"
