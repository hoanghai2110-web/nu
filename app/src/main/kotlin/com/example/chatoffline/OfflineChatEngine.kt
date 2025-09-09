
package com.example.chatoffline

import kotlin.random.Random

class OfflineChatEngine {
    
    // Placeholder responses - sẽ được thay thế bằng llama.cpp sau này
    private val placeholderResponses = listOf(
        "Tôi hiểu bạn đang nói về điều đó.",
        "Đó là một quan điểm thú vị!",
        "Bạn có thể kể thêm chi tiết không?",
        "Tôi đồng ý với bạn.",
        "Hmm, để tôi suy nghĩ về điều này...",
        "Cảm ơn bạn đã chia sẻ!",
        "Điều đó rất có ý nghĩa.",
        "Bạn có kinh nghiệm gì về vấn đề này?",
        "Tôi chưa từng nghĩ về nó theo cách đó.",
        "Bạn nói đúng đấy!"
    )

    // Điểm móc cho JNI/NDK integration sau này
    external fun initializeNativeEngine(): Boolean
    external fun generateResponseNative(input: String): String
    
    companion object {
        init {
            try {
                // Sẽ load native library khi có NDK
                // System.loadLibrary("chatengine")
            } catch (e: UnsatisfiedLinkError) {
                // Chưa có native library, sử dụng placeholder
            }
        }
    }

    fun generateResponse(userMessage: String): String {
        // TODO: Thay thế bằng native call khi có llama.cpp
        // return generateResponseNative(userMessage)
        
        // Placeholder logic đơn giản
        return when {
            userMessage.contains("xin chào", ignoreCase = true) -> "Xin chào! Tôi có thể giúp gì cho bạn?"
            userMessage.contains("cảm ơn", ignoreCase = true) -> "Không có gì! Tôi luôn sẵn sàng giúp đỡ."
            userMessage.contains("tạm biệt", ignoreCase = true) -> "Tạm biệt! Hẹn gặp lại bạn!"
            userMessage.length < 10 -> "Bạn có thể nói rõ hơn được không?"
            else -> placeholderResponses[Random.nextInt(placeholderResponses.size)]
        }
    }

    fun isNativeEngineAvailable(): Boolean {
        // Sẽ check native engine khi có NDK
        return false
    }
}
