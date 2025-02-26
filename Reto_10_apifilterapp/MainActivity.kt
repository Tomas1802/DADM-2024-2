package co.edu.unal.apifilterapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton: Button = findViewById(R.id.searchButton)
        val inputYear: EditText = findViewById(R.id.inputYear)
        val inputSex: EditText = findViewById(R.id.inputSex)
        val inputEstrato: EditText = findViewById(R.id.inputEstrato)
        val resultText: TextView = findViewById(R.id.resultText)

        searchButton.setOnClickListener {
            val year = inputYear.text.toString()
            val sex = inputSex.text.toString()
            val estrato = inputEstrato.text.toString()
            fetchFilteredData(year, sex, estrato, resultText)
        }
    }

    private fun fetchFilteredData(year: String, sex: String, estrato: String, resultText: TextView) {
        val client = OkHttpClient()

        // Build URL with dynamic filters
        val baseUrl = "https://www.datos.gov.co/resource/nk8x-s9hw.json"
        val queryParams = mutableListOf<String>()

        if (year.isNotEmpty()) queryParams.add("id_ano=$year")
        if (sex.isNotEmpty()) queryParams.add("sexo=$sex")
        if (estrato.isNotEmpty()) queryParams.add("estrato_socioeconomico=$estrato")

        val url = if (queryParams.isNotEmpty()) "$baseUrl?${queryParams.joinToString("&")}" else baseUrl

        val request = Request.Builder().url(url).build()

        thread {
            try {
                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()
                val jsonArray = JSONArray(jsonData)
                val results = StringBuilder()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    results.append("Año: ").append(item.optString("id_ano", "N/A"))
                        .append("\nEdad: ").append(item.optString("edad_de_la_victima", "N/A"))
                        .append("\nSexo: ").append(item.optString("sexo", "N/A"))
                        .append("\nEstrato Socioeconómico: ").append(item.optString("estrato_socioeconomico", "N/A"))
                        .append("\nNúmero de Intentos: ").append(item.optString("numero_de_intentos", "N/A"))
                        .append("\nEstado Civil: ").append(item.optString("estado_civil", "N/A"))
                        .append("\nConflicto con Pareja o Ex: ").append(item.optString("conflicto_con_pareja_o_ex", "N/A"))
                        .append("\n\n")
                }

                runOnUiThread {
                    if (results.isNotEmpty()) {
                        resultText.text = results.toString()
                    } else {
                        resultText.text = "No data found for the given filters."
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    resultText.text = "Error fetching data."
                }
            }
        }
    }
}