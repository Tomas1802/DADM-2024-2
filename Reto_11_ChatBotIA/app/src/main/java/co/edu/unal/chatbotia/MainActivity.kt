package co.edu.unal.chatbotia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import co.edu.unal.chatbotia.viewmodel.ChatViewModel
import co.edu.unal.chatbotia.R

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.chatbotia.adapter.ChatAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Initialize the adapter with an empty list
        chatAdapter = ChatAdapter(mutableListOf())
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        chatViewModel.chatMessages.observe(this) { messages ->
            val previousSize = chatAdapter.itemCount
            chatAdapter.updateMessages(messages)

            rvChat.postDelayed({
                if (messages.isNotEmpty()) {
                    rvChat.smoothScrollToPosition(messages.size - 1)
                }
            }, 100) // Small delay to ensure UI update before scrolling
        }

        chatViewModel.isLoading.observe(this) { isLoading ->
            btnSend.isEnabled = !isLoading
        }

        btnSend.setOnClickListener {
            val userMessage = etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                chatViewModel.sendMessage(userMessage)
                etMessage.text.clear()
            }
        }
    }
}
