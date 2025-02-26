package models
import java.io.Serializable

data class Company(
    val id: Int,
    val name: String,
    val url: String,
    val phone: String,
    val email: String,
    val products: String,
    val category: String
) : Serializable