
# Hướng dẫn build APK trên VPS Ubuntu 24.04

## Thông số VPS
- **OS**: Ubuntu 24.04.3 LTS (Noble)
- **CPU**: 4 vCPU Intel Broadwell @ 2.0GHz (KVM)
- **RAM**: 7.8 GiB (còn trống ~3 GiB)
- **Disk**: 142 GB (còn trống ~120 GB)
- **Java**: OpenJDK 17.0.16
- **Android SDK**: /root/android-sdk

## Chuẩn bị môi trường (nếu chưa có)

### 1. Cài đặt Java Development Kit
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

### 2. Kiểm tra Android SDK
```bash
ls -la /root/android-sdk/
echo $ANDROID_SDK_ROOT
echo $ANDROID_HOME
```

### 3. Set environment variables (nếu chưa có)
```bash
echo 'export ANDROID_SDK_ROOT=/root/android-sdk' >> ~/.bashrc
echo 'export ANDROID_HOME=/root/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin' >> ~/.bashrc
source ~/.bashrc
```

## Build APK

### 1. Upload source code lên VPS
```bash
# Scp hoặc git clone project lên VPS
# Hoặc tạo thủ công từ files đã cung cấp
```

### 2. Cấp quyền execute cho script
```bash
chmod +x build_apk.sh
```

### 3. Chạy build script
```bash
./build_apk.sh
```

### 4. Kết quả
Script sẽ tạo ra:
- **APK file**: `output/ChatOffline_YYYYMMDD_HHMMSS.apk`
- **ZIP package**: `ChatOffline_YYYYMMDD_HHMMSS.zip` (chứa source + APK)

## Cấu trúc project

```
ChatOffline/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/example/chatoffline/
│       │   │   ├── MainActivity.kt
│       │   │   ├── Message.kt
│       │   │   ├── MessageAdapter.kt
│       │   │   └── OfflineChatEngine.kt
│       │   ├── res/
│       │   │   ├── drawable/
│       │   │   ├── layout/
│       │   │   ├── values/
│       │   │   └── ...
│       │   └── cpp/
│       │       └── CMakeLists.txt (chuẩn bị cho JNI)
├── build.gradle
├── settings.gradle
├── gradle.properties
├── local.properties
├── build_apk.sh
└── README_VPS.md
```

## Tính năng hiện tại

✅ **Đã implement:**
- UI đơn giản với danh sách tin nhắn, ô nhập, nút gửi
- Chat engine offline với placeholder responses
- RecyclerView hiển thị tin nhắn
- Scroll tự động xuống tin nhắn mới

🚧 **Chuẩn bị sẵn cho tương lai:**
- JNI/NDK integration point trong `OfflineChatEngine.kt`
- CMakeLists.txt cho native code
- ProGuard rules cho native methods

## Troubleshooting

### Lỗi "SDK not found"
```bash
export ANDROID_SDK_ROOT=/root/android-sdk
export ANDROID_HOME=/root/android-sdk
```

### Lỗi "Permission denied"
```bash
chmod +x gradlew
chmod +x build_apk.sh
```

### Lỗi "Out of memory"
```bash
# Tăng heap size cho Gradle
echo "org.gradle.jvmargs=-Xmx4g" >> gradle.properties
```

### Kiểm tra build tools
```bash
/root/android-sdk/cmdline-tools/latest/bin/sdkmanager --list | grep build-tools
```

## Tích hợp llama.cpp (tương lai)

Khi cần tích hợp llama.cpp:

1. Uncomment phần NDK trong `app/build.gradle`
2. Implement native methods trong `app/src/main/cpp/`
3. Update `OfflineChatEngine.kt` để sử dụng native calls
4. Add llama.cpp source vào native build

## Kiểm tra kết quả

Sau khi build thành công:
- APK có thể install trên Android device (API level 21+)
- App sẽ hoạt động offline hoàn toàn
- Chat engine trả về responses từ placeholder list

---

**Lưu ý**: Project này được thiết kế để dễ dàng mở rộng với AI models như llama.cpp trong tương lai.
