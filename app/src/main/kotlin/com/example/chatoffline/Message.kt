
package com.example.chatoffline

data class Message(
    val id: Long,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val isFromUser: Boolean = true
)
