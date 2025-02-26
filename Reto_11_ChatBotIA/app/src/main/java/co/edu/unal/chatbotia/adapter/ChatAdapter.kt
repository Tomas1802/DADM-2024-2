package co.edu.unal.chatbotia.adapter

import android.graphics.Color
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.chatbotia.R
import co.edu.unal.chatbotia.network.Message

class ChatAdapter(private var messages: MutableList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = Html.fromHtml(message.content) // Render Markdown as HTML

        // Styling based on sender
        if (message.role == "user") {
            holder.tvMessage.setBackgroundResource(R.drawable.user_bubble)
            holder.tvMessage.gravity = Gravity.END
        } else {
            holder.tvMessage.setBackgroundResource(R.drawable.assistant_bubble)
            holder.tvMessage.gravity = Gravity.START
        }
    }

    override fun getItemCount(): Int = messages.size

    // ✅ Update messages dynamically instead of re-creating adapter
    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}