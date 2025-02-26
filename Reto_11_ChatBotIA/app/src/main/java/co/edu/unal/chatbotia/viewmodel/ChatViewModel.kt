package co.edu.unal.chatbotia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.edu.unal.chatbotia.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel : ViewModel() {

    private val _chatMessages = MutableLiveData<List<Message>>(mutableListOf())
    val chatMessages: LiveData<List<Message>> get() = _chatMessages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun sendMessage(userMessage: String) {
        _isLoading.value = true

        // Obtener el historial actual del chat
        val currentMessages = _chatMessages.value?.toMutableList() ?: mutableListOf()

        // Agregar el mensaje del usuario al historial
        currentMessages.add(Message(role = "user", content = userMessage))
        _chatMessages.value = currentMessages

        val request = ChatRequest(messages = currentMessages)

        RetrofitInstance.api.getChatResponse(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val assistantMessage = response.body()?.choices?.firstOrNull()?.message
                    assistantMessage?.let {
                        currentMessages.add(it)
                        _chatMessages.value = currentMessages
                    }
                } else {
                    currentMessages.add(Message(role = "assistant", content = "Error en la respuesta de OpenAI."))
                    _chatMessages.value = currentMessages
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                _isLoading.value = false
                currentMessages.add(Message(role = "assistant", content = "Error de conexión: ${t.message}"))
                _chatMessages.value = currentMessages
            }
        })
    }
}