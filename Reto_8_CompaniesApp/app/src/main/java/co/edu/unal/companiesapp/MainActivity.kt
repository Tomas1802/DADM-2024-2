package co.edu.unal.companiesapp

import CompanyAdapter
import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import models.Company

class MainActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    filterCompanies(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    loadCompanies() // Reload all companies if the search query is empty
                } else {
                    filterCompanies(newText)
                }
                return true
            }
        })

        val fabAddCompany: FloatingActionButton = findViewById(R.id.fabAddCompany)
        fabAddCompany.setOnClickListener {
            val intent = Intent(this, AddCompanyActivity::class.java)
            startActivity(intent)
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseHelper = DatabaseHelper(this)

        loadCompanies()
    }

    override fun onResume() {
        super.onResume()
        loadCompanies()
    }

    private fun loadCompanies() {
        val cursor = databaseHelper.getAllCompanies()
        val companies = mutableListOf<Company>()

        if (cursor.moveToFirst()) {
            do {
                companies.add(
                    Company(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        url = cursor.getString(2),
                        phone = cursor.getString(3),
                        email = cursor.getString(4),
                        products = cursor.getString(5),
                        category = cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = CompanyAdapter(companies,
            onDelete = { company -> showDeleteConfirmationDialog(company) },
            onEdit = { company ->
                val intent = Intent(this, UpdateCompanyActivity::class.java)
                intent.putExtra("company", company)
                startActivity(intent)
            }
        )
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
    }



    private fun showDeleteConfirmationDialog(company: Company) {
        AlertDialog.Builder(this)
            .setTitle("Delete Company")
            .setMessage("Are you sure you want to delete ${company.name}?")
            .setPositiveButton("Yes") { _, _ ->
                val rowsDeleted = databaseHelper.deleteCompany(company.id)
                if (rowsDeleted > 0) {
                    Toast.makeText(this, "Company deleted", Toast.LENGTH_SHORT).show()
                    loadCompanies() // Refresh the list
                } else {
                    Toast.makeText(this, "Error deleting company", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun filterCompanies(query: String) {
        val cursor = databaseHelper.filterCompanies(query, query) // Use the same query for name and category
        val filteredCompanies = mutableListOf<Company>()

        if (cursor.moveToFirst()) {
            do {
                filteredCompanies.add(
                    Company(
                        id = cursor.getInt(0),
                        name = cursor.getString(1),
                        url = cursor.getString(2),
                        phone = cursor.getString(3),
                        email = cursor.getString(4),
                        products = cursor.getString(5),
                        category = cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = CompanyAdapter(
            filteredCompanies,
            onDelete = { company -> showDeleteConfirmationDialog(company) }, // Handle delete action
            onEdit = { company -> // Handle edit action
                val intent = Intent(this, UpdateCompanyActivity::class.java)
                intent.putExtra("company", company) // Pass the company object
                startActivity(intent)
            }
        )
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter
    }


}