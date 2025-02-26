import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.companiesapp.R
import models.Company

class CompanyAdapter(
    private val companies: List<Company>,
    private val onDelete: (Company) -> Unit,
    private val onEdit: (Company) -> Unit // Edit callback
) : RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val category: TextView = view.findViewById(R.id.tvCategory)
        val phone: TextView = view.findViewById(R.id.tvPhone)
        val editButton: Button = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        holder.name.text = company.name
        holder.category.text = company.category
        holder.phone.text = company.phone

        // Trigger edit callback
        holder.editButton.setOnClickListener {
            onEdit(company)
        }

        // Trigger delete callback
        holder.itemView.setOnLongClickListener {
            onDelete(company)
            true
        }
    }

    override fun getItemCount(): Int = companies.size
}

