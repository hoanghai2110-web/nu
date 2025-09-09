
#!/bin/bash

echo "=== Bắt đầu build APK Chat Offline ==="
echo "Thời gian: $(date)"
echo "Hệ điều hành: $(lsb_release -d | cut -f2)"
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Android SDK: $ANDROID_SDK_ROOT"
echo

# Kiểm tra môi trường
if [ ! -d "/root/android-sdk" ]; then
    echo "❌ Lỗi: Android SDK không tìm thấy tại /root/android-sdk"
    exit 1
fi

if [ -z "$ANDROID_SDK_ROOT" ]; then
    export ANDROID_SDK_ROOT=/root/android-sdk
    echo "✅ Đã set ANDROID_SDK_ROOT=/root/android-sdk"
fi

if [ -z "$ANDROID_HOME" ]; then
    export ANDROID_HOME=/root/android-sdk
    echo "✅ Đã set ANDROID_HOME=/root/android-sdk"
fi

# Add Android tools to PATH
export PATH=$PATH:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin

# Set JAVA_OPTS to avoid JVM conflicts
export JAVA_OPTS="-Xmx2g -Xms512m"
export GRADLE_OPTS="-Xmx2g -Xms512m -Dfile.encoding=UTF-8"

echo "🔧 Chuẩn bị build..."

# Kiểm tra và set quyền cho gradlew
if [ ! -f "gradlew" ]; then
    echo "❌ Lỗi: gradlew không tồn tại!"
    exit 1
fi

echo "🔧 Setting gradlew permissions..."
chmod +x gradlew

if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "📥 Downloading Gradle wrapper jar..."
    mkdir -p gradle/wrapper
    wget -O gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.0.2/gradle/wrapper/gradle-wrapper.jar
fi

# Validate gradle wrapper
echo "🔍 Validating Gradle..."
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "❌ Lỗi: gradle-wrapper.jar không tồn tại!"
    exit 1
fi

# Test gradle
echo "🧪 Testing Gradle..."
./gradlew --version
if [ $? -ne 0 ]; then
    echo "❌ Lỗi: Gradle không hoạt động!"
    exit 1
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean --no-daemon

# Build debug APK
echo "🔨 Building debug APK..."
./gradlew assembleDebug --no-daemon

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build thành công!"
    
    # Find APK file
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📱 APK được tạo tại: $APK_PATH"
        echo "📏 Kích thước APK: $APK_SIZE"
        
        # Create output directory
        mkdir -p output
        
        # Copy APK to output directory with timestamp
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        OUTPUT_APK="output/ChatOffline_${TIMESTAMP}.apk"
        cp "$APK_PATH" "$OUTPUT_APK"
        echo "💾 APK đã được copy to: $OUTPUT_APK"
        
        # Create zip package
        ZIP_FILE="ChatOffline_${TIMESTAMP}.zip"
        echo "📦 Tạo package zip..."
        
        zip -r "$ZIP_FILE" \
            app/ \
            build.gradle \
            settings.gradle \
            gradle.properties \
            local.properties \
            build_apk.sh \
            README_VPS.md \
            "$OUTPUT_APK" \
            -x "*/build/*" "*/.*" "*/.gradle/*"
        
        if [ -f "$ZIP_FILE" ]; then
            ZIP_SIZE=$(du -h "$ZIP_FILE" | cut -f1)
            echo "✅ Package hoàn thành: $ZIP_FILE"
            echo "📏 Kích thước ZIP: $ZIP_SIZE"
            echo
            echo "🎉 Hoàn thành! Files đã tạo:"
            echo "   - APK: $OUTPUT_APK"
            echo "   - Source + APK: $ZIP_FILE"
        else
            echo "❌ Lỗi tạo ZIP file"
        fi
        
    else
        echo "❌ Không tìm thấy APK file tại $APK_PATH"
        exit 1
    fi
else
    echo "❌ Build failed!"
    echo "🔍 Chi tiết lỗi:"
    ./gradlew assembleDebug --stacktrace --no-daemon
    exit 1
fi

echo
echo "=== Build hoàn thành ==="
echo "Thời gian kết thúc: $(date)"
