package co.edu.unal.companiesapp

import DatabaseHelper
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCompanyActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_company)

        databaseHelper = DatabaseHelper(this)

        val etName: EditText = findViewById(R.id.etName)
        val etUrl: EditText = findViewById(R.id.etUrl)
        val etPhone: EditText = findViewById(R.id.etPhone)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etProducts: EditText = findViewById(R.id.etProducts)
        val etCategory: EditText = findViewById(R.id.etCategory)
        val btnSave: Button = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val url = etUrl.text.toString()
            val phone = etPhone.text.toString()
            val email = etEmail.text.toString()
            val products = etProducts.text.toString()
            val category = etCategory.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && category.isNotEmpty()) {
                val result = databaseHelper.insertCompany(name, url, phone, email, products, category)
                if (result > 0) {
                    Toast.makeText(this, "Company added successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity and return to the previous screen
                } else {
                    Toast.makeText(this, "Error adding company", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Name, Phone, and Category are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
