package ca.doophie.nhlroster.adapters

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.doophie.nhlroster.R
import ca.doophie.nhlroster.models.Player
import ca.doophie.nhlroster.services.NHLService
import kotlinx.android.synthetic.main.list_item_player.view.*

class PlayerView(view: View) : RecyclerView.ViewHolder(view) {
    var player: Player? = null
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value ?: return

            itemView.name_text.text = value.name

            itemView.position_text.text = "Position: ${value.pos}"
            itemView.points_text.text = "Points: ${value.points ?: "Unknown"}"
            itemView.number_text.text = if (value.number >= 0)
                "# ${value.number} - "
            else
                "# N/a - "

            if (player?.portrait == null) {
                NHLService.getPlayerPortrait(value) {
                    player?.portrait = it

                    Handler(Looper.getMainLooper()).post {
                        itemView.player_portrait.setImageBitmap(it)
                    }
                }
            } else {
                itemView.player_portrait.setImageBitmap(player?.portrait)
            }
        }
}

class RosterAdapter(var players: List<Player>, private val onPlayerSelected:(PlayerView)->Unit) : RecyclerView.Adapter<PlayerView>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerView {
        return PlayerView(LayoutInflater.from(parent.context).inflate(R.layout.list_item_player, parent, false))
    }

    override fun getItemCount(): Int {
        return players.count()
    }

    override fun onBindViewHolder(holder: PlayerView, position: Int) {
        holder.player = players[position]

        holder.itemView.player_portrait.transitionName = "PlayerPortrait$position"
        holder.itemView.player_details.transitionName = "PlayerDetails$position"

        holder.itemView.setOnClickListener {
            onPlayerSelected(holder)
        }
    }

}