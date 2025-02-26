package co.edu.unal.companiesapp

import DatabaseHelper
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unal.companiesapp.R
import models.Company

class UpdateCompanyActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_company)

        databaseHelper = DatabaseHelper(this)

        val etName: EditText = findViewById(R.id.etName)
        val etUrl: EditText = findViewById(R.id.etUrl)
        val etPhone: EditText = findViewById(R.id.etPhone)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etProducts: EditText = findViewById(R.id.etProducts)
        val etCategory: EditText = findViewById(R.id.etCategory)
        val btnUpdate: Button = findViewById(R.id.btnUpdate)

        // Retrieve company data from intent
        val company = intent.getSerializableExtra("company") as Company

        etName.setText(company.name)
        etUrl.setText(company.url)
        etPhone.setText(company.phone)
        etEmail.setText(company.email)
        etProducts.setText(company.products)
        etCategory.setText(company.category)

        btnUpdate.setOnClickListener {
            val updatedCompany = company.copy(
                name = etName.text.toString(),
                url = etUrl.text.toString(),
                phone = etPhone.text.toString(),
                email = etEmail.text.toString(),
                products = etProducts.text.toString(),
                category = etCategory.text.toString()
            )

            val rowsAffected = databaseHelper.updateCompany(
                updatedCompany.id,
                updatedCompany.name,
                updatedCompany.url,
                updatedCompany.phone,
                updatedCompany.email,
                updatedCompany.products,
                updatedCompany.category
            )

            if (rowsAffected > 0) {
                Toast.makeText(this, "Company updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error updating company", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
