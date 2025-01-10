import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.edu.unal.tictactoe.R
import co.edu.unal.tictactoe.models.Game

class GameAdapter(
    private val games: List<Game>,
    private val onJoinGameClick: (String) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewGameId: TextView = view.findViewById(R.id.textViewGameId)
        val btnJoin: Button = view.findViewById(R.id.btnJoinGame)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.textViewGameId.text = "Game ID: ${game.id}"
        holder.btnJoin.setOnClickListener { onJoinGameClick(game.id) }
    }

    override fun getItemCount(): Int = games.size
}
