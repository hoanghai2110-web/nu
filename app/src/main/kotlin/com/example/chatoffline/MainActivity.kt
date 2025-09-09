
package com.example.chatoffline

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatEngine: OfflineChatEngine
    private val messages = mutableListOf<Message>()
    private var messageIdCounter = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupChatEngine()
        setupClickListeners()
        
        // Tin nhắn chào mừng
        addBotMessage("Xin chào! Tôi là chat bot offline. Hãy nói gì đó với tôi!")
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
    }

    private fun setupChatEngine() {
        chatEngine = OfflineChatEngine()
    }

    private fun setupClickListeners() {
        buttonSend.setOnClickListener {
            sendMessage()
        }

        editTextMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            // Thêm tin nhắn người dùng
            addUserMessage(messageText)
            
            // Xóa text trong EditText
            editTextMessage.text.clear()
            
            // Tạo phản hồi từ bot (sau 1 giây để giống thật)
            recyclerView.postDelayed({
                val botResponse = chatEngine.generateResponse(messageText)
                addBotMessage(botResponse)
            }, 1000)
        }
    }

    private fun addUserMessage(content: String) {
        val message = Message(
            id = ++messageIdCounter,
            sender = "Bạn",
            content = content,
            timestamp = System.currentTimeMillis(),
            isFromUser = true
        )
        messageAdapter.addMessage(message)
        scrollToBottom()
    }

    private fun addBotMessage(content: String) {
        val message = Message(
            id = ++messageIdCounter,
            sender = "Bot",
            content = content,
            timestamp = System.currentTimeMillis(),
            isFromUser = false
        )
        messageAdapter.addMessage(message)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
    }
}
