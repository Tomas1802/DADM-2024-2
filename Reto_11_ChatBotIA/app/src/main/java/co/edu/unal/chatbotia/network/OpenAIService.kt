package co.edu.unal.chatbotia.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Modelo de solicitud a OpenAI
data class ChatRequest(
    val model: String = "gpt-4o",
    val messages: List<Message>
)

// Modelo de respuesta de OpenAI
data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val role: String,
    val content: String
)

// Interfaz para la API de OpenAI
interface OpenAIService {
    @Headers(
        "Content-Type: application/json",
    )
    @POST("v1/chat/completions")
    fun getChatResponse(@Body request: ChatRequest): Call<ChatResponse>
}

// Singleton para Retrofit
object RetrofitInstance {
    private const val BASE_URL = "https://api.openai.com/"

    val api: OpenAIService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }
}