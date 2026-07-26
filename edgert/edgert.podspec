Pod::Spec.new do |spec|
    spec.name                     = 'edgert'
    spec.version                  = '1.0'
    spec.homepage                 = 'https://github.com/ShadAdman/edgert'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Core abstractions and native dependencies for EdgeRt'
    spec.vendored_frameworks      = 'build/cocoapods/framework/edgert.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '16.0'
    spec.dependency 'TensorFlowLiteObjC'
    spec.dependency 'TensorFlowLiteObjC/CoreML'
    spec.dependency 'TensorFlowLiteObjC/Metal'
    spec.dependency 'executorch'
                
    if !Dir.exist?('build/cocoapods/framework/edgert.framework') || Dir.empty?('build/cocoapods/framework/edgert.framework')
        raise "

        Kotlin framework 'edgert' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :edgert:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':edgert',
        'PRODUCT_MODULE_NAME' => 'edgert',
    }
                
    spec.script_phases = [
        {
            :name => 'Build edgert',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end