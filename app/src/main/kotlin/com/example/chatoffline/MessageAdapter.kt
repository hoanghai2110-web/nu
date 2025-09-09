
package com.example.chatoffline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: MutableList<Message>) : 
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewSender: TextView = itemView.findViewById(R.id.textViewSender)
        val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.textViewSender.text = message.sender
        holder.textViewMessage.text = message.content
        holder.textViewTime.text = dateFormat.format(Date(message.timestamp))
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
